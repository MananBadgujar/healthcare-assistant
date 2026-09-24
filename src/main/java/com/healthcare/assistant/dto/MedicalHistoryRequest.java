package com.healthcare.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for medical‑history intake requests.
 * Used by {@code RecordHistoryController} POST endpoint.
 */
public class MedicalHistoryRequest {

    @NotBlank(message = "Type is required")
    @Size(max = 100, message = "Type must be less than 100 characters")
    private String type;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must be less than 2000 characters")
    private String content;

    public MedicalHistoryRequest() {}

    public MedicalHistoryRequest(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}