package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DosageInteraction;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DosageInteractionService {

    /**
     * Check a pair of medications for dosage-related interactions.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param interactions list to add detected interactions to
     */
    public void dosageInteractionCheck(Medication medicationA,
                                       Medication medicationB,
                                       List<DosageInteraction> interactions) {
        // Skip if same medication (compare by IDs if available)
        if (medicationA.getId() != null && medicationB.getId() != null
                && medicationA.getId().equals(medicationB.getId())) {
            return;
        }
        // Also check by name as fallback for non-persisted medications
        if (medicationA.getName() != null && medicationB.getName() != null
                && medicationA.getName().equalsIgnoreCase(medicationB.getName())) {
            return;
        }

        // Duplicate dosing check
        duplicateDosingCheck(medicationA, medicationB, interactions);

        // Dosing range conflict check
        dosingRangeConflictCheck(medicationA, medicationB, interactions);

        // Sort by severity (most severe first)
        interactions.sort(Comparator.comparing((DosageInteraction di) -> di.getSeverity())
                .reversed());
    }

    /**
     * Check for duplicate dosing (same medication administered at overlapping doses).
     */
    private void duplicateDosingCheck(Medication medicationA,
                                     Medication medicationB,
                                     List<DosageInteraction> interactions) {
        if (medicationA.getName() == null || medicationB.getName() == null) {
            return;
        }

        String nameA = medicationA.getName().toLowerCase();
        String nameB = medicationB.getName().toLowerCase();

        // Check for same drug class with duplicate dosing
        if (nameA.equals(nameB)) {
            DosageInteraction interaction = new DosageInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "Duplicate dosing: same medication " + medicationA.getName() + " prescribed twice.",
                    "Review dosing schedule; avoid duplicate administration.",
                    "DUPLICATE_DOSING",
                    "Clinical rule: duplicate dosing detection"
            );
            interactions.add(interaction);
            return;
        }

        // Check for drugs with same active ingredient
        if (hasSameActiveIngredient(nameA, nameB)) {
            DosageInteraction interaction = new DosageInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "Potential duplicate active ingredient dosing between " + medicationA.getName()
                            + " and " + medicationB.getName() + ".",
                    "Review dosing; avoid concurrent administration of same active ingredient.",
                    "DUPLICATE_DOSING",
                    "Clinical rule: duplicate active ingredient detection"
            );
            interactions.add(interaction);
        }
    }

    /**
     * Check if two medication names have the same active ingredient.
     */
    private boolean hasSameActiveIngredient(String nameA, String nameB) {
        // Simple check - in a full implementation, this would use a drug database
        // to determine if two names refer to the same active ingredient
        String[] commonPairs = {"ibuprofen-advil", "ibuprofen-motrin",
                                "acetaminophen-tylenol", "naproxen-aleve"};
        for (String pair : commonPairs) {
            String[] parts = pair.split("-");
            if ((nameA.contains(parts[0]) && nameB.contains(parts[1]))
                    || (nameA.contains(parts[1]) && nameB.contains(parts[0]))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for dosing range conflicts between two medications.
     */
    private void dosingRangeConflictCheck(Medication medicationA,
                                          Medication medicationB,
                                          List<DosageInteraction> interactions) {
        if (medicationA.getName() == null || medicationB.getName() == null) {
            return;
        }

        String nameA = medicationA.getName().toLowerCase();
        String nameB = medicationB.getName().toLowerCase();

        // Example: Two medications with overlapping maximum dosing limits
        // that could lead to accidental overdose
        if (isHighRiskMedication(nameA) && isHighRiskMedication(nameB)) {
            DosageInteraction interaction = new DosageInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "Both medications are high-risk; dosing range overlap may increase overdose risk.",
                    "Review total daily dose; monitor for signs of overdose; consider dose adjustment.",
                    "DOSING_RANGE_CONFLICT",
                    "Clinical rule: dosing range conflict detection"
            );
            interactions.add(interaction);
        }

        // Example: Medication A's dose combined with Medication B exceeds recommended maximum
        if (dosagesExceedMaximum(nameA, nameB)) {
            DosageInteraction interaction = new DosageInteraction(
                    medicationA, medicationB,
                    "CRITICAL",
                    "Combined dosing of " + medicationA.getName() + " and " + medicationB.getName()
                            + " exceeds recommended maximum daily dose.",
                    "Reduce individual doses; calculate total daily dose; monitor toxicity signs.",
                    "DOSING_RANGE_CONFLICT",
                    "Clinical rule: maximum daily dose exceeded"
            );
            interactions.add(interaction);
        }
    }

    /**
     * Check if a medication name indicates a high-risk medication.
     */
    private boolean isHighRiskMedication(String name) {
        String[] highRisk = {"warfarin", "insulin", "morphine", "fentanyl", "digoxin", "phenytoin"};
        for (String risk : highRisk) {
            if (name.contains(risk)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if combined dosages exceed maximum recommended daily dose.
     */
    private boolean dosagesExceedMaximum(String nameA, String nameB) {
        // Simplified check - in full implementation would look up actual dosage strengths
        // and calculate combined total against recommended maximums
        if (nameA.contains("warfarin") && nameB.contains("aspirin")) {
            return true; // Example: combined bleeding risk
        }
        if (nameA.contains("acetaminophen") && nameB.contains("acetaminophen")) {
            return true; // Example: duplicate acetaminophen dosing
        }
        return false;
    }
}