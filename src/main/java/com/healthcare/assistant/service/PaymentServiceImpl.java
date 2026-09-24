package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Payment;
import com.healthcare.assistant.entity.enums.PaymentStatus;
import com.healthcare.assistant.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPayment(Long id) {
        Optional<Payment> opt = paymentRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getInvoice() != null && p.getInvoice().getId().equals(invoiceId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Payment updatePaymentStatus(Long id, String status) {
        Payment payment = getPayment(id);
        if (payment != null) {
            // Assuming we have a method to set paymentStatus; but PaymentStatus is enum.
            // For simplicity, we will match string to enum.
            try {
                var newStatus = PaymentStatus.valueOf(status.toUpperCase());
                payment.setPaymentStatus(newStatus);
                return paymentRepository.save(payment);
            } catch (IllegalArgumentException e) {
                return null; // invalid status
            }
        }
        return null;
    }

    @Override
    public Payment refundPayment(Long id, BigDecimal refundAmount) {
        Payment payment = getPayment(id);
        if (payment != null) {
            payment.setRefundAmount(refundAmount);
            payment.setPaymentStatus(PaymentStatus.REFUNDED); // simple handling
            return paymentRepository.save(payment);
        }
        return null;
    }
}