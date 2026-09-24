package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateInsuranceRequest {
    @NotBlank
    private String providerName;
    @NotBlank
    private String policyNumber;
    @NotBlank
    private String memberId;
    @NotBlank
    private String planName;

    // Getters and Setters
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
}