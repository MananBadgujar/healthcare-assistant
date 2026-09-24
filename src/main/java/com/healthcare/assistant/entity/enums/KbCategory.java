package com.healthcare.assistant.entity.enums;

/**
 * Categories used to classify knowledge-base documents.
 * <p>
 * Categories drive metadata filtering during semantic retrieval so that a
 * patient-education query can be restricted to a relevant category (e.g. only
 * medications or only chronic conditions).
 */
public enum KbCategory {
    DISEASES,
    SYMPTOMS,
    MEDICATIONS,
    NUTRITION,
    LIFESTYLE,
    PREVENTION,
    PROCEDURES,
    PATIENT_INSTRUCTIONS,
    CHRONIC_CONDITIONS,
    GENERAL_HEALTH_EDUCATION
}
