package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.BillingRequest;
import com.healthcare.assistant.dto.BillingResponse;
import com.healthcare.assistant.service.BillingService;
import com.healthcare.assistant.service.PatientContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private PatientContextService patientContextService;

    @PostMapping
    public ResponseEntity<String> createInvoice(@RequestBody BillingRequest request) {
        String id = billingService.createInvoice(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<BillingResponse> getInvoice(@PathVariable String invoiceId) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        BillingResponse response = billingService.getInvoice(invoiceId);
        if (response.getPatientId() != null && !response.getPatientId().equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BillingResponse>> getInvoicesByPatient(@PathVariable Long patientId) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!patientId.equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<BillingResponse> invoices = billingService.getInvoicesByPatientId(patientId);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/{invoiceId}/pay")
    public ResponseEntity<String> markAsPaid(@PathVariable String invoiceId) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        BillingResponse response = billingService.getInvoice(invoiceId);
        if (response.getPatientId() != null && !response.getPatientId().equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        billingService.markAsPaid(invoiceId);
        return ResponseEntity.ok("Paid");
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<BillingResponse>> getInvoicesByProvider(@PathVariable Long providerId) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (currentPatientId != null && !providerId.equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<BillingResponse> invoices = billingService.getInvoicesByProviderId(providerId);
        return ResponseEntity.ok(invoices);
    }
}