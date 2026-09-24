package com.healthcare.inventoryservice.web;

import com.healthcare.inventoryservice.common.AuditService;
import com.healthcare.inventoryservice.common.EventPublisher;
import com.healthcare.inventoryservice.entity.StockItem;
import com.healthcare.inventoryservice.repo.StockRepository;
import com.healthcare.contracts.DomainEvent;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final StockRepository repo;
    private final EventPublisher events;
    private final AuditService audit;
    public InventoryController(StockRepository repo, EventPublisher events, AuditService audit) {
        this.repo = repo; this.events = events; this.audit = audit;
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<StockItem> create(@RequestBody StockItem item, Authentication auth) {
        if (item.getSku() == null || item.getSku().isBlank()) throw new IllegalArgumentException("sku required");
        item.setId(null);
        StockItem saved = repo.save(item);
        audit.record(auth.getName(), "STOCK_CREATE", "StockItem", String.valueOf(saved.getId()), "SUCCESS");
        maybeEmit(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/items/{id}/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public StockItem stockOut(@PathVariable Long id, @RequestBody Map<String, Integer> body, Authentication auth) {
        StockItem item = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Item not found"));
        int qty = body.getOrDefault("quantity", 1);
        if (qty <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (item.getQuantity() < qty) throw new IllegalArgumentException("Insufficient stock");
        item.setQuantity(item.getQuantity() - qty);
        StockItem saved = repo.save(item);
        audit.record(auth.getName(), "STOCK_OUT", "StockItem", String.valueOf(id), "SUCCESS");
        maybeEmit(saved);
        return saved;
    }

    @GetMapping("/reorder-analysis")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public List<Map<String, Object>> reorder() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (StockItem i : repo.findAll()) {
            if (i.getQuantity() <= i.getReorderThreshold())
                out.add(Map.of("sku", i.getSku(), "quantity", i.getQuantity(),
                        "recommendation", "REORDER", "forecast7d", Math.max(0, i.getReorderThreshold() * 2 - i.getQuantity())));
        }
        return out;
    }

    @GetMapping("/expiry-alerts")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public List<Map<String, Object>> expiry() {
        LocalDate soon = LocalDate.now().plusDays(30);
        List<Map<String, Object>> out = new ArrayList<>();
        for (StockItem i : repo.findAll()) {
            if (i.getExpiryDate() != null && !i.getExpiryDate().isAfter(soon))
                out.add(Map.of("sku", i.getSku(), "expiryDate", String.valueOf(i.getExpiryDate()), "alert", "NEAR_EXPIRY"));
        }
        return out;
    }

    private void maybeEmit(StockItem item) {
        try {
            if (item.getQuantity() <= item.getReorderThreshold())
                events.publish("healthcare.inventory.events.low-stock",
                        DomainEvent.of("inventory.low-stock", "StockItem", String.valueOf(item.getId()),
                                "inventory-service", MDC.get("correlationId"),
                                Map.of("sku", item.getSku() == null ? "" : item.getSku())));
        } catch (Exception ignored) {}
    }
}
