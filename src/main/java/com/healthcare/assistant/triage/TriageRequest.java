package com.healthcare.assistant.triage;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for health symptom query.
 */
@Data
public class TriageRequest {
    @NotBlank
    private String question;
    private Integer age;
    private String gender;
    private List<String> symptoms;
    private String duration; // e.g., "3 days"
    private String severity; // e.g., "mild"
    private String context;  // e.g., "work from home"
}