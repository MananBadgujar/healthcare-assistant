package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateInsuranceStatusRequest {
    @NotBlank
    private String status;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}