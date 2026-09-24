package com.healthcare.assistant.triage.result;

import lombok.Data;
import java.util.List;

/**
 * Structured result of symptom extraction and clinical safety check.
 */
@Data
public class SymptomExtractionResult {
    private List<String> extractedSymptoms;
    private String identifiedCondition;
    private String severity;
    private boolean redFlag;
    private boolean emergency;
    private String medicationSuggestion;
    private String clinicalGuidance;
}