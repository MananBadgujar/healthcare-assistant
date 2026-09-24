package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateClaimStatusRequest {
    @NotBlank
    private String status;

    @NotBlank
    private String rejectionReason; // optional, required when rejecting

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}