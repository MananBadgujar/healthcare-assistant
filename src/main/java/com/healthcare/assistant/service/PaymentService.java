package com.healthcare.assistant.service;


import java.math.BigDecimal;
import java.util.List;

import com.healthcare.assistant.entity.Payment;
public interface PaymentService {
    Payment createPayment(Payment payment);
    Payment getPayment(Long id);
    List<Payment> getPaymentsByInvoiceId(Long invoiceId);
    Payment updatePaymentStatus(Long id, String status);
    Payment refundPayment(Long id, BigDecimal refundAmount);
}