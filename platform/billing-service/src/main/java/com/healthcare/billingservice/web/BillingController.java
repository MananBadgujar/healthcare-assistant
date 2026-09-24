package com.healthcare.billingservice.web;

import com.healthcare.billingservice.common.AuditService;
import com.healthcare.billingservice.common.EventPublisher;
import com.healthcare.billingservice.entity.Claim;
import com.healthcare.billingservice.entity.Invoice;
import com.healthcare.billingservice.repo.ClaimRepository;
import com.healthcare.billingservice.repo.InvoiceRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {
    private static final Set<String> PAY = Set.of("PENDING>PROCESSING", "PROCESSING>PAID", "PROCESSING>FAILED", "PENDING>CANCELLED", "FAILED>CANCELLED");
    private final InvoiceRepository invoices;
    private final ClaimRepository claims;
    private final EventPublisher events;
    private final AuditService audit;

    public BillingController(InvoiceRepository invoices, ClaimRepository claims, EventPublisher events, AuditService audit) {
        this.invoices = invoices; this.claims = claims; this.events = events; this.audit = audit;
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Invoice> create(@RequestBody Invoice inv, Authentication auth) {
        if (inv.getAmount() == null || inv.getAmount() <= 0) throw new IllegalArgumentException("amount must be positive");
        inv.setId(null); inv.setStatus("PENDING");
        Invoice saved = invoices.save(inv);
        audit.record(auth.getName(), "INVOICE_CREATE", "Invoice", String.valueOf(saved.getId()), "SUCCESS");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/invoices/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Invoice pay(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        Invoice inv = invoices.findById(id).orElseThrow(() -> new NoSuchElementException("Invoice not found"));
        boolean ok = !"fail".equalsIgnoreCase(body.getOrDefault("outcome", "ok"));
        String step1 = next(inv.getStatus(), "PROCESSING");
        inv.setStatus(step1); invoices.save(inv);
        String terminal = ok ? "PAID" : "FAILED";
        inv.setStatus(next(inv.getStatus(), terminal));
        Invoice saved = invoices.save(inv);
        audit.record(auth.getName(), "PAY_" + terminal, "Invoice", String.valueOf(id), "SUCCESS");
        try {
            events.publish(Topics.PAYMENT_COMPLETED, DomainEvent.of("billing.payment-" + terminal.toLowerCase(),
                    "Invoice", String.valueOf(id), "billing-service", MDC.get("correlationId"),
                    Map.of("status", terminal, "amount", String.valueOf(saved.getAmount()))));
        } catch (Exception ignored) {}
        return saved;
    }

    private String next(String from, String to) {
        if (!PAY.contains(from + ">" + to)) throw new IllegalArgumentException("Illegal transition " + from + " -> " + to);
        return to;
    }

    @PostMapping("/claims")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Claim> claim(@RequestBody Claim c, Authentication auth) {
        invoices.findById(c.getInvoiceId()).orElseThrow(() -> new NoSuchElementException("Invoice not found"));
        c.setId(null); c.setStatus("SUBMITTED");
        Claim saved = claims.save(c);
        audit.record(auth.getName(), "CLAIM_SUBMIT", "Claim", String.valueOf(saved.getId()), "SUCCESS");
        try {
            events.publish(Topics.CLAIM_SUBMITTED, DomainEvent.of("claim.submitted", "Claim",
                    String.valueOf(saved.getId()), "billing-service", MDC.get("correlationId"), Map.of()));
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
