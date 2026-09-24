package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePreAuthorizationStatusRequest {
    @NotBlank
    private String status;

    private String rejectionReason; // optional

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}