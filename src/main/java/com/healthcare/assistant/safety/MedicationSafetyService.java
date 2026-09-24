package com.healthcare.assistant.safety;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

/**
 * Centralized medication safety guardrails.
 * Defines which medication names or classes are prohibited or allowed.
 * In a real system this would be backed by a database and pharmacological rules.
 */
@Service
public class MedicationSafetyService {

    private final Set<String> prohibitedMedications;

    public MedicationSafetyService() {
        // Example prohibited list; extend as needed per clinical policy
        this.prohibitedMedications = new HashSet<>();
        this.prohibitedMedications.add("isosorbide");
        this.prohibitedMedications.add("sildenafil");
        // Add more as required by clinical safety policy
    }

    /**
     * Returns {@code true} if the medication name is not prohibited.
     *
     * @param medicationName name of medication
     * @return true if safe to suggest
     */
    public boolean isMedicationAllowed(String medicationName) {
        return medicationName != null && !prohibitedMedications.contains(medicationName.toLowerCase());
    }

    /**
     * Returns a safety comment that can be appended to AI‑generated guidance.
     *
     * @param medicationName medication being considered
     * @return safety note or empty string if no special warning
     */
    public String getSafetyNote(String medicationName) {
        if (medicationName != null && !isMedicationAllowed(medicationName)) {
            return " NOTIFIED: Prohibited medication – clinical review required.";
        }
        return "";
    }

    /**
     * Returns the set of prohibited medications.
     *
     * @return unmodifiable set of prohibited medication names
     */
    public Set<String> getProhibitedMedications() {
        return java.util.Collections.unmodifiableSet(prohibitedMedications);
    }

    /**
     * Determines if the supplied guidance contains unsafe self‑medication language
     * (e.g., dosage instructions that are not part of official prescription).
     *
     * @param guidance raw AI‑generated guidance text
     * @return true if unsafe self‑medication detected
     */
    public boolean isSelfMedicationUnsafe(String guidance) {
        if (guidance == null) {
            return false;
        }
        String lower = guidance.toLowerCase();
        // Look for dosage/strength patterns combined with prescription verbs
        return lowermatchesDosagePattern(lower) && containsPrescriptionVerb(lower);
    }

    private boolean lowermatchesDosagePattern(String lower) {
        // Pattern: a number followed by a unit that can be dosage
        return lower.matches(".*\\d+\\s*(mg|ml|tablet|pill|dosage|strength|frequency|unit)\\b.*");
    }

    private boolean containsPrescriptionVerb(String lower) {
        // Common verbs indicating prescription instructions
        String[] verbs = {"prescribe", "prescription", "start", "stop", "take", "dose", "frequency", "strength"};
        for (String v : verbs) {
            if (lower.contains(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects if the guidance suggests a prescription or dosage change that
     * should only be made by a qualified clinician.
     *
     * @param guidance raw AI‑generated guidance text
     * @return true if prescription‑type language detected
     */
    public boolean isPrescriptionSuggestion(String guidance) {
        if (guidance == null) {
            return false;
        }
        String lower = guidance.toLowerCase();
        // key terms that indicate prescribing behavior
        String[] prescribingTerms = {"prescribe", "dosage", "strength", "frequency", "take", "use", "apply"};
        for (String term : prescribingTerms) {
            if (lower.contains(term)) {
                return true;
            }
        }
        return false;
    }
}