package com.healthcare.assistant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreatePreAuthorizationRequest {
    @NotNull
    private Long patientId;
    @NotNull
    private Long insuranceId;
    @NotNull
    private Long invoiceId; // optional may be null
    @NotBlank
    private String medicalInformation;
    @NotBlank
    private String serviceInformation;
    @NotNull
    @Pattern(regexp = "PENDING|SUBMITTED|APPROVED|REJECTED|EXPIRED")
    private String status; // PreAuthorizationStatus enum name
    @NotNull
    private java.math.BigDecimal requestedAmount;

    // Getters and Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getInsuranceId() { return insuranceId; }
    public void setInsuranceId(Long insuranceId) { this.insuranceId = insuranceId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getMedicalInformation() { return medicalInformation; }
    public void setMedicalInformation(String medicalInformation) { this.medicalInformation = medicalInformation; }
    public String getServiceInformation() { return serviceInformation; }
    public void setServiceInformation(String serviceInformation) { this.serviceInformation = serviceInformation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.math.BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(java.math.BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
}