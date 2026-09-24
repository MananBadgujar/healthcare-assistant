package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.CreatePaymentRequest;
import com.healthcare.assistant.dto.UpdatePaymentStatusRequest;
import com.healthcare.assistant.entity.Payment;
import com.healthcare.assistant.entity.enums.PaymentMethod;
import com.healthcare.assistant.entity.enums.PaymentStatus;
import com.healthcare.assistant.entity.Invoice;
import com.healthcare.assistant.service.PaymentService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        Payment payment = paymentService.createPayment(mapToEntity(request));
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {

        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<Payment>> getPaymentsByInvoiceId(
            @PathVariable Long invoiceId) {

        List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
        return ResponseEntity.ok(payments);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        Payment updated =
                paymentService.updatePaymentStatus(id, request.getStatus());

        if (request.getRejectionReason() != null && !request.getRejectionReason().trim().isEmpty()) {

            updated.setRejectionReason(request.getRejectionReason());
        }

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody BigDecimal refundAmount) {

        Payment refunded =
                paymentService.refundPayment(id, refundAmount);

        return ResponseEntity.ok(refunded);
    }

    private Payment mapToEntity(CreatePaymentRequest req) {

        Payment payment = new Payment();

        payment.setInvoice(
                new Invoice()
        );

        payment.setPatient(
                new com.healthcare.assistant.entity.Patient()
        );

        payment.setAmount(req.getAmount());

        PaymentMethod paymentMethod = req.getMethod() != null ? req.getMethod() : PaymentMethod.CASH;
        payment.setPaymentMethod(paymentMethod);

        PaymentStatus paymentStatus = req.getStatus() != null ? req.getStatus() : PaymentStatus.PENDING;
        payment.setPaymentStatus(paymentStatus);

        return payment;
    }
}