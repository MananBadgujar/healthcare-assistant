package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.BillingRequest;
import com.healthcare.assistant.dto.BillingResponse;
import com.healthcare.assistant.entity.Billing;

import java.util.List;

public interface BillingService {
    String createInvoice(BillingRequest request);
    BillingResponse getInvoice(String invoiceId);
    List<BillingResponse> getInvoicesByPatientId(Long patientId);
    void markAsPaid(String invoiceId);
    List<BillingResponse> getInvoicesByProviderId(Long providerId);
    Billing saveBilling(Billing billing);
}