package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteraction;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Service for deterministic drug interaction detection using clinical rules.
 * <p>
 * This service does NOT use AI/LLM to determine interaction safety.
 * All interactions are classified via established clinical rules with
 * explicit evidence references. AI assistance is advisory only and
 * must pass through deterministic safety validation.
 */
@Service
public class DrugInteractionService {

    /**
     * Check a pair of medications for interactions.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @return list of detected interactions (may be empty if none detected)
     */
    public List<DrugInteraction> checkInteractions(Medication medicationA, Medication medicationB) {
        List<DrugInteraction> interactions = new ArrayList<>();

        // Skip if same medication (compare by name if IDs are not available)
        if (medicationA.getId() != null && medicationB.getId() != null
                && medicationA.getId().equals(medicationB.getId())) {
            return interactions;
        }
        // Also check by name as fallback for non-persisted medications
        if (medicationA.getName() != null && medicationB.getName() != null
                && medicationA.getName().equalsIgnoreCase(medicationB.getName())) {
            return interactions;
        }

        // Therapeutic duplication check
        therapeuticDuplicationCheck(medicationA, medicationB, interactions);

        // Metabolic interaction check (CYP450 system)
        metabolicInteractionCheck(medicationA, medicationB, interactions);

        // Administration interaction check (route, timing, food interactions)
        administrationInteractionCheck(medicationA, medicationB, interactions);

        // Sort by severity (most severe first)
        interactions.sort(Comparator.comparing((DrugInteraction di) -> di.getSeverity())
                .reversed());

        return interactions;
    }

    /**
     * Check for administration-related interactions (route, timing, food).
     */
    private void administrationInteractionCheck(Medication medicationA,
                                                Medication medicationB,
                                                List<DrugInteraction> interactions) {
        // Example: Warfarin + Aspirin - increased bleeding risk via administration
        if (isWarfarin(medicationA) && isAspirin(medicationB)) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "Concurrent administration of aspirin with warfarin increases bleeding risk.",
                    "Monitor INR; consider alternative antiplatelet.",
                    "ADMINISTRATION_INTERACTION"
            );
            interactions.add(interaction);
        }
        if (isAspirin(medicationA) && isWarfarin(medicationB)) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "Concurrent administration of aspirin with warfarin increases bleeding risk.",
                    "Monitor INR; consider alternative antiplatelet.",
                    "ADMINISTRATION_INTERACTION"
            );
            interactions.add(interaction);
        }
        // Example: Antacids + Fluoroquinolones - absorption interference
        if (medicationA.getName() != null && medicationB.getName() != null
                && medicationA.getName().toLowerCase().contains("antacid")
                && medicationB.getName().toLowerCase().contains("ciprofloxacin")) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "MODERATE",
                    "Antacids may decrease absorption of fluoroquinolones.",
                    "Separate administration by at least 2 hours.",
                    "ADMINISTRATION_INTERACTION"
            );
            interactions.add(interaction);
        }
        if (medicationA.getName() != null && medicationB.getName() != null
                && medicationA.getName().toLowerCase().contains("ciprofloxacin")
                && medicationB.getName().toLowerCase().contains("antacid")) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "MODERATE",
                    "Antacids may decrease absorption of fluoroquinolones.",
                    "Separate administration by at least 2 hours.",
                    "ADMINISTRATION_INTERACTION"
            );
            interactions.add(interaction);
        }
    }

    /**
     * Check if a medication is aspirin.
     */
    private boolean isAspirin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("aspirin") || name.contains("acetylsalicylic");
    }

    /**
     * Check for therapeutic duplication (same drug class, same indication).
     */
    private void therapeuticDuplicationCheck(Medication medicationA,
                                             Medication medicationB,
                                             List<DrugInteraction> interactions) {
        // Example: Two anticoagulants - increased bleeding risk
        boolean aIsAnticoagulant = isAnticoagulant(medicationA);
        boolean bIsAnticoagulant = isAnticoagulant(medicationB);

        if (aIsAnticoagulant && bIsAnticoagulant) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "Concurrent use of two anticoagulants increases bleeding risk.",
                    "Review anticoagulation therapy; monitor coagulation parameters.",
                    "THERAPEUTIC_DUPLICATION"
            );
            interactions.add(interaction);
        }

        // Example: Two NSAIDs - increased GI risk
        boolean aIsNsaid = isNsaid(medicationA);
        boolean bIsNsaid = isNsaid(medicationB);

        if (aIsNsaid && bIsNsaid) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "MODERATE",
                    "Concurrent use of multiple NSAIDs increases gastrointestinal risk.",
                    "Consider gastroprotection or alternative analgesic.",
                    "THERAPEUTIC_DUPLICATION"
            );
            interactions.add(interaction);
        }
    }

    /**
     * Check for metabolic interactions via CYP450 enzyme system.
     */
    private void metabolicInteractionCheck(Medication medicationA,
                                           Medication medicationB,
                                           List<DrugInteraction> interactions) {
        // Example: Fluconazole (CYP2C9 inhibitor) + Warfarin
        if (isCyp2C9Inhibitor(medicationA) && isWarfarin(medicationB)) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "CRITICAL",
                    "CYP2C9 inhibitor (fluconazole) increases warfarin levels; risk of bleeding.",
                    "Reduce warfarin dose; monitor INR closely; consider alternative.",
                    "METABOLIC_INTERACTION"
            );
            interactions.add(interaction);
        }
        // Reverse direction
        if (isCyp2C9Inhibitor(medicationB) && isWarfarin(medicationA)) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "CRITICAL",
                    "CYP2C9 inhibitor increases warfarin levels; risk of bleeding.",
                    "Reduce warfarin dose; monitor INR closely; consider alternative.",
                    "METABOLIC_INTERACTION"
            );
            interactions.add(interaction);
        }

        // Example: Rifampin (CYP2C9 inducer) + Warfarin
        if (isCyp2C9Inducer(medicationA) && isWarfarin(medicationB)) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "CYP2C9 inducer (rifampin) decreases warfarin levels; risk of therapeutic failure.",
                    "Increase warfarin dose; monitor INR closely.",
                    "METABOLIC_INTERACTION"
            );
            interactions.add(interaction);
        }
        // Reverse direction
        if (isCyp2C9Inducer(medicationB) && isWarfarin(medicationA)) {
            DrugInteraction interaction = new DrugInteraction(
                    medicationA, medicationB,
                    "HIGH",
                    "CYP2C9 inducer decreases warfarin levels; risk of therapeutic failure.",
                    "Increase warfarin dose; monitor INR closely.",
                    "METABOLIC_INTERACTION"
            );
            interactions.add(interaction);
        }
    }

    /**
     * Check for drug-allergy interactions.
     *
     * @param medication the medication to check
     * @param allergy      description of the allergy
     * @return list of detected drug-allergy interactions
     */
    public List<DrugInteraction> checkDrugAllergyInteraction(Medication medication, String allergy) {
        List<DrugInteraction> interactions = new ArrayList<>();

        if (allergy == null || allergy.isBlank()) {
            return interactions;
        }

        String allergyLower = allergy.toLowerCase();

        // Example: Penicillin allergy + certain cephalosporins (cross-reactivity)
        if (isPenicillinAllergy(allergyLower) && isCephalosporin(medication)) {
            DrugInteraction interaction = new DrugInteraction(
                    medication, null,
                    "HIGH",
                    "Potential cross-reactivity between penicillin allergy and cephalosporin.",
                    "Avoid cephalosporin if severe penicillin allergy; consider alternative antibiotic.",
                    "DRUG_ALLERGY"
            );
            interactions.add(interaction);
        }

        // Example: Shellfish allergy + contrast media
        if (isShellfishAllergy(allergyLower) && isIodinatedContrast(medication)) {
            DrugInteraction interaction = new DrugInteraction(
                    medication, null,
                    "MODERATE",
                    "Shellfish allergy may increase risk of adverse reaction to iodinated contrast.",
                    "Pre-medicate with steroids and antihistamines; consider non-iodinated contrast.",
                    "DRUG_ALLERGY"
            );
            interactions.add(interaction);
        }

        return interactions;
    }

    /**
     * Check interactions for a medication against a set of other medications.
     *
     * @param medication the medication to check
     * @param otherMeds  other medications to check against
     * @return list of all detected interactions
     */
    public List<DrugInteraction> checkAllInteractions(Medication medication, Set<Medication> otherMeds) {
        List<DrugInteraction> allInteractions = new ArrayList<>();

        for (Medication other : otherMeds) {
            if (other.getId() != null && medication.getId() != null
                    && other.getId().equals(medication.getId())) {
                continue; // skip same medication
            }
            // Also check by name as fallback
            if (other.getName() != null && medication.getName() != null
                    && other.getName().equalsIgnoreCase(medication.getName())) {
                continue; // skip same medication by name
            }
            List<DrugInteraction> interactions = checkInteractions(medication, other);
            allInteractions.addAll(interactions);
        }

        return allInteractions;
    }

    /**
     * Compare severity strings for sorting.
     */
    private int severityCompare(String a, String b) {
        // CRITICAL > HIGH > MODERATE > LOW > NONE
        String[] order = {"CRITICAL", "HIGH", "MODERATE", "LOW", "NONE"};
        int idxA = -1, idxB = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(a)) idxA = i;
            if (order[i].equals(b)) idxB = i;
        }
        return Integer.compare(idxB, idxA); // descending order
    }

    // Helper methods for clinical rule detection

    private boolean isAnticoagulant(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("warfarin") || name.contains("heparin") ||
                name.contains("enoxaparin") || name.contains("dalteparin") ||
                name.contains("fondaparinux");
    }

    private boolean isNsaid(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("ibuprofen") || name.contains("naproxen") ||
                name.contains("diclofenac") || name.contains("indomethacin") ||
                name.contains("ketorolac") || name.contains("meloxicam");
    }

    private boolean isCyp2C9Inhibitor(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("fluconazole");
    }

    private boolean isCyp2C9Inducer(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("rifampin") || name.contains("carbamazepine") ||
                name.contains("phenobarbital") || name.contains("phenytoin");
    }

    private boolean isWarfarin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("warfarin");
    }

    private boolean isPenicillinAllergy(String allergy) {
        return allergy.contains("penicillin") || allergy.contains("penicillins") ||
                allergy.contains("penam") || allergy.contains("penicillin allergy");
    }

    private boolean isShellfishAllergy(String allergy) {
        return allergy.contains("shellfish") || allergy.contains("shrimp") ||
                allergy.contains("crab") || allergy.contains("mollusk");
    }

    private boolean isCephalosporin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("cef") || name.contains("cefaclor") ||
                name.contains("cefpodoxime") || name.contains("ceftriaxone") ||
                name.contains("cefuroxime");
    }

    private boolean isIodinatedContrast(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("contrast") || name.contains("iohexol") ||
                name.contains("iopamidol") || name.contains("iodine");
    }
}