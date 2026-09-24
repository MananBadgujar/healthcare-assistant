package com.healthcare.assistant.entity.enums;

/**
 * Category of drug interaction.
 */
public enum InteractionCategory {
    DRUG_DRUG,
    DRUG_CONDITION,
    DRUG_ALLERGY,
    THERAPEUTIC_DUPLICATION,
    DOSE_DEPENDENT,
    QUANTITATIVE_INTERACTION,
    METABOLIC_INTERACTION,
    ADMINISTRATION_INTERACTION,
    UNKNOWN
}