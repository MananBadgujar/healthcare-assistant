package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.SymptomExtraction;
import com.healthcare.assistant.entity.TriageRecord;
import com.healthcare.assistant.service.ClinicalRuleEngine;
import com.healthcare.assistant.service.SymptomExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simple implementation that returns a new TriageRecord without detailed population.
 * Further population will be added in subsequent steps.
 */
@Service
public class ClinicalRuleEngineService implements ClinicalRuleEngine {

    private final SymptomExtractionService extractionService;

    @Autowired
    public ClinicalRuleEngineService(SymptomExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @Override
    public TriageRecord assess(SymptomExtraction extraction) {
        // For now, just return an empty TriageRecord.
        // Subsequent steps will populate urgency, flags, recommendations, etc.
        return new TriageRecord();
    }
}