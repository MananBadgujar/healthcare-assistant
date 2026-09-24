package com.healthcare.assistant.triage.service;

import com.healthcare.assistant.triage.TriageRequest;
import com.healthcare.assistant.triage.result.SymptomExtractionResult;

/**
 * Service that extracts structured symptom information and applies clinical safety rules.
 */
public interface SymptomExtractionService {
    /**
     * Performs NER-based symptom extraction and safety validation.
     *
     * @param request user query containing health information
     * @return structured result including red-flag detection and guidance
     */
    SymptomExtractionResult extractSymptoms(TriageRequest request);
}