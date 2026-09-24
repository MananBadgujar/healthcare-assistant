package com.healthcare.assistant.dto;

public class ReminderScheduleRequest {
    private Long patientId;
    private String reminderType;
    private String scheduledDateTime;

    public ReminderScheduleRequest() {}

    public ReminderScheduleRequest(Long patientId, String reminderType, String scheduledDateTime) {
        this.patientId = patientId;
        this.reminderType = reminderType;
        this.scheduledDateTime = scheduledDateTime;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }

    public String getScheduledDateTime() { return scheduledDateTime; }
    public void setScheduledDateTime(String scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }
}