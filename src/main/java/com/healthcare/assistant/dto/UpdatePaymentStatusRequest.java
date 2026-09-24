package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePaymentStatusRequest {
    @NotBlank
    private String status;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    private String rejectionReason;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}