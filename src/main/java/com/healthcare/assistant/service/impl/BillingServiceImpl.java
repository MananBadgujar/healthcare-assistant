package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.BillingRequest;
import com.healthcare.assistant.dto.BillingResponse;
import com.healthcare.assistant.entity.Billing;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.entity.enums.InvoiceStatus;
import com.healthcare.assistant.repository.BillingRepository;
import com.healthcare.assistant.repository.ProviderRepository;
import com.healthcare.assistant.repository.PatientRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("billingServiceImpl2")
public class BillingServiceImpl implements com.healthcare.assistant.service.BillingService {

    private final BillingRepository billingRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;

    @Autowired
    public BillingServiceImpl(BillingRepository billingRepository,
                              PatientRepository patientRepository,
                              ProviderRepository providerRepository) {
        this.billingRepository = billingRepository;
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    public String createInvoice(BillingRequest request) {
        // Resolve patient
        Optional<Patient> patientOpt = patientRepository.findById(request.getPatientId());
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found: " + request.getPatientId());
        }
        Patient patient = patientOpt.get();

        // Resolve provider
        Optional<Provider> providerOpt = providerRepository.findById(request.getProviderId());
        if (!providerOpt.isPresent()) {
            throw new RuntimeException("Provider not found: " + request.getProviderId());
        }
        Provider provider = providerOpt.get();

        // Create Billing entity
        Billing billing = new Billing();

        // Set relationships
        billing.setPatient(patient);
        billing.setProvider(provider);

        // Map request fields to entity fields
        billing.setBillingType(request.getBillingType());
        billing.setDescription(request.getDescription());
        billing.setCurrency(request.getCurrency());
        billing.setDateOfService(request.getDateOfService());

        // Set monetary fields
        billing.setServiceAmount(request.getAmount());
        billing.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        billing.setTax(request.getTax() != null ? request.getTax() : BigDecimal.ZERO);
        billing.setAdjustment(request.getAdjustment() != null ? request.getAdjustment() : BigDecimal.ZERO);

        // Calculate total amount: serviceAmount - discount + tax + adjustment
        BigDecimal total = billing.getServiceAmount()
                .subtract(billing.getDiscount())
                .add(billing.getTax())
                .add(billing.getAdjustment());
        billing.setTotalAmount(total);
        // Set amount field for backward compatibility (same as totalAmount)
        billing.setAmount(total);

        // Set status to PENDING for new invoice
        billing.setStatus(InvoiceStatus.PENDING);
        billing.setBillingDate(LocalDateTime.now());

        // Set issuedDate to current date
        billing.setIssuedDate(LocalDate.now());

        // Save the billing entity
        Billing savedBilling = billingRepository.save(billing);

        // Return the generated ID as string
        return savedBilling.getId().toString();
    }

    @Override
    public BillingResponse getInvoice(String invoiceId) {
        // Parse invoiceId to Long
        try {
            Long id = Long.parseLong(invoiceId);
            Optional<Billing> billingOpt = billingRepository.findById(id);
            if (!billingOpt.isPresent()) {
                throw new RuntimeException("Invoice not found: " + invoiceId);
            }
            Billing billing = billingOpt.get();

            // Map entity to response
            BillingResponse response = new BillingResponse();
            response.setId(billing.getId().toString());
            response.setPatientId(billing.getPatient().getId());
            response.setProviderId(billing.getProvider().getId());
            response.setAmount(billing.getAmount());
            response.setServiceAmount(billing.getServiceAmount());
            response.setDiscount(billing.getDiscount());
            response.setTax(billing.getTax());
            response.setAdjustment(billing.getAdjustment());
            response.setTotalAmount(billing.getTotalAmount());
            response.setBillingType(billing.getBillingType());
            response.setDescription(billing.getDescription());
            response.setDate(billing.getDateOfService());
            response.setCurrency(billing.getCurrency());
            response.setIssuedDate(billing.getIssuedDate());
            response.setDueDate(billing.getDueDate());
            response.setStatus(billing.getStatus().name());
            response.setStatusMessage(billing.getStatus().name());

            return response;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid invoice ID format: " + invoiceId, e);
        }
    }

    @Override
    public List<BillingResponse> getInvoicesByPatientId(Long patientId) {
        List<Billing> billings = billingRepository.findByPatientId(patientId);
        return billings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BillingResponse> getInvoicesByProviderId(Long providerId) {
        List<Billing> billings = billingRepository.findByProviderId(providerId);
        return billings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsPaid(String invoiceId) {
        try {
            Long id = Long.parseLong(invoiceId);
            Optional<Billing> opt = billingRepository.findById(id);
            if (!opt.isPresent()) {
                throw new RuntimeException("Invoice not found: " + invoiceId);
            }
            Billing billing = opt.get();
            billing.setStatus(InvoiceStatus.PAID);
            billingRepository.save(billing);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid invoice ID format: " + invoiceId, e);
        }
    }

    @Override
    public Billing saveBilling(Billing billing) {
        return billingRepository.save(billing);
    }

    // Helper method to map Billing to BillingResponse
    private BillingResponse toResponse(Billing billing) {
        BillingResponse response = new BillingResponse();
        response.setId(billing.getId().toString());
        response.setPatientId(billing.getPatient().getId());
        response.setProviderId(billing.getProvider().getId());
        response.setAmount(billing.getAmount());
        response.setServiceAmount(billing.getServiceAmount());
        response.setDiscount(billing.getDiscount());
        response.setTax(billing.getTax());
        response.setAdjustment(billing.getAdjustment());
        response.setTotalAmount(billing.getTotalAmount());
        response.setBillingType(billing.getBillingType());
        response.setDescription(billing.getDescription());
        response.setDate(billing.getDateOfService());
        response.setCurrency(billing.getCurrency());
        response.setIssuedDate(billing.getIssuedDate());
        response.setDueDate(billing.getDueDate());
        response.setStatus(billing.getStatus().name());
        response.setStatusMessage(billing.getStatus().name());
        return response;
    }
}