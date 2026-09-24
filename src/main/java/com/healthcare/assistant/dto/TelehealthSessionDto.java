package com.healthcare.assistant.dto;

import java.time.LocalDateTime;
import com.healthcare.assistant.entity.TelehealthSession;

public class TelehealthSessionDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long providerId;
    private String providerName;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private String status;
    private String meetingUrl;
    private String sessionReference;
    private String notes;

    // Default constructor for JSON deserialization of @RequestBody payloads
    public TelehealthSessionDto() {
    }

    // Constructor from entity
    public TelehealthSessionDto(TelehealthSession session) {
        this.id = session.getId();
        // patient/populated from security context in real implementation
        this.status = session.getStatus();
        this.scheduledStart = session.getScheduledStart();
        this.scheduledEnd = session.getScheduledEnd();
        this.actualStart = session.getActualStart();
        this.actualEnd = session.getActualEnd();
        this.meetingUrl = session.getMeetingUrl();
        this.sessionReference = session.getSessionReference();
        this.notes = session.getNotes();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(LocalDateTime scheduledStart) { this.scheduledStart = scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(LocalDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }
    public LocalDateTime getActualStart() { return actualStart; }
    public void setActualStart(LocalDateTime actualStart) { this.actualStart = actualStart; }
    public LocalDateTime getActualEnd() { return actualEnd; }
    public void setActualEnd(LocalDateTime actualEnd) { this.actualEnd = actualEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }
    public String getSessionReference() { return sessionReference; }
    public void setSessionReference(String sessionReference) { this.sessionReference = sessionReference; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}