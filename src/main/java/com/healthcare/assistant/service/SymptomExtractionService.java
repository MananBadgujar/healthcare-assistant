package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.SymptomExtraction;

/**
 * Service for extracting symptoms from user input.
 */
public interface SymptomExtractionService {

    /**
     * Extracts structured symptom information from the given user message.
     *
     * @param userMessage the raw user message
     * @return populated SymptomExtraction entity
     */
    SymptomExtraction extractSymptoms(String userMessage);
}