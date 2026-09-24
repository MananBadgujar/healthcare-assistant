package com.healthcare.assistant.dto;

public class RefillRequest {
    private Long patientId;
    private Long medicationId;
    private Integer requestedQuantity;
    private String reason;

    // Getters and Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}