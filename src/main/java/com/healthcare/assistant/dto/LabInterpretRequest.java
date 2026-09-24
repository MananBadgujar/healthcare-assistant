package com.healthcare.assistant.dto;

public class LabInterpretRequest {
    private Long patientId;
    private String testName;

    public LabInterpretRequest() {}

    public LabInterpretRequest(Long patientId, String testName) {
        this.patientId = patientId;
        this.testName = testName;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
}