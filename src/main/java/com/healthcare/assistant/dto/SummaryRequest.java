package com.healthcare.assistant.dto;

public class SummaryRequest {
    private Long patientId;
    private String content;

    public SummaryRequest() {}

    public SummaryRequest(Long patientId, String content) {
        this.patientId = patientId;
        this.content = content;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}