package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.SymptomExtraction;
import com.healthcare.assistant.entity.TriageRecord;

/**
 * Service for applying clinical rules to extracted symptom data and generating triage records.
 */
public interface ClinicalRuleEngine {

    /**
     * Analyzes symptom extraction, applies clinical rules, and builds a TriageRecord.
     *
     * @param extraction structured symptom data
     * @return populated TriageRecord reflecting clinical assessment
     */
    TriageRecord assess(SymptomExtraction extraction);
}