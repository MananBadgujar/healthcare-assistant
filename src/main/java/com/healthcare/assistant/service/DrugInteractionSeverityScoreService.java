package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionSeverityScore;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DrugInteractionSeverityScoreService {

    /**
     * Calculate a severity score (0-10) for the interaction between two medications.
     * <p>
     * Consider factors like: therapeutic duplication, metabolic interactions,
     * administration interactions, allergy interactions.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param scores      existing severity score records for reference
     * @return calculated severity score (0-10)
     */
    public Integer calculateSeverityScore(Medication medicationA, Medication medicationB,
                                          List<DrugInteractionSeverityScore> scores) {
        int score = 0;

        // Check for therapeutic duplication (same drug class)
        if (isAnticoagulant(medicationA) && isAnticoagulant(medicationB)) {
            score += 4;
        }
        if (isNsaid(medicationA) && isNsaid(medicationB)) {
            score += 3;
        }

        // Check for metabolic interactions (CYP450 system)
        if (isCyp2C9Inhibitor(medicationA) && isWarfarin(medicationB)) {
            score += 5;
        }
        if (isCyp2C9Inhibitor(medicationB) && isWarfarin(medicationA)) {
            score += 5;
        }

        // Check for administration interactions
        if (isWarfarin(medicationA) && isAspirin(medicationB)) {
            score += 4;
        }
        if (isAspirin(medicationA) && isWarfarin(medicationB)) {
            score += 4;
        }

        // Ensure score is within 0-10 range
        if (score > 10) {
            score = 10;
        }

        return score;
    }

    // Helper methods for clinical rule detection

    private boolean isAnticoagulant(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("warfarin") || name.contains("heparin")
                || name.contains("enoxaparin") || name.contains("dalteparin")
                || name.contains("fondaparinux");
    }

    private boolean isNsaid(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("ibuprofen") || name.contains("naproxen")
                || name.contains("diclofenac") || name.contains("indomethacin")
                || name.contains("ketorolac") || name.contains("meloxicam");
    }

    private boolean isCyp2C9Inhibitor(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("fluconazole");
    }

    private boolean isWarfarin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("warfarin");
    }

    private boolean isAspirin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("aspirin") || name.contains("acetylsalicylic");
    }
}