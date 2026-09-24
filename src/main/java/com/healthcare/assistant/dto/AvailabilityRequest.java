package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotNull;

public class AvailabilityRequest {
    @NotNull
    private Boolean available;

    public AvailabilityRequest() {}

    public AvailabilityRequest(Boolean available) {
        this.available = available;
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}