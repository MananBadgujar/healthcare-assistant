package com.healthcare.assistant.dto;

import java.time.LocalDate;

public class SuggestAppointmentRequest {
    private Long patientId;
    private Long providerId;
    private LocalDate preferredDate;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }
}
