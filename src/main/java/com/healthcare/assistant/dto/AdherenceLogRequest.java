package com.healthcare.assistant.dto;

import java.time.LocalDateTime;

public class AdherenceLogRequest {
    private Long patientId;
    private Long medicationId;
    private LocalDateTime loggedAt;
    private Boolean taken;
    private String notes;

    // Getters and Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    public Boolean getTaken() { return taken; }
    public void setTaken(Boolean taken) { this.taken = taken; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}