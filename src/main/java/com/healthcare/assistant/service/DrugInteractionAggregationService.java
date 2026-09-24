package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteraction;
import com.healthcare.assistant.entity.DrugInteractionAggregation;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DrugInteractionAggregationService {

    /**
     * Aggregate all interaction types between two medications and categorize them
     * by severity.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param aggregations list of existing aggregation records to incorporate
     * @return aggregated interaction data categorized by severity
     */
    public DrugInteractionAggregation aggregateInteractions(Medication medicationA,
                                                              Medication medicationB,
                                                              List<DrugInteractionAggregation> aggregations) {
        List<DrugInteraction> allInteractions = new ArrayList<>();
        int severeInteractionsCount = 0;
        int highCount = 0;
        int moderateCount = 0;
        int mildCount = 0;

        // Skip if same medication (compare by IDs if available)
        if (medicationA.getId() != null && medicationB.getId() != null
                && medicationA.getId().equals(medicationB.getId())) {
            DrugInteractionAggregation aggregation = new DrugInteractionAggregation(
                    medicationA, medicationB);
            aggregation.setTotalInteractions(0);
            aggregation.setSevereInteractions(0);
            aggregation.setModerateInteractions(0);
            aggregation.setMildInteractions(0);
            aggregation.setOverallSeverity("NONE");
            aggregation.setInteractionCategories("");
            return aggregation;
        }

        // Also check by name as fallback for non-persisted medications
        if (medicationA.getName() != null && medicationB.getName() != null
                && medicationA.getName().equalsIgnoreCase(medicationB.getName())) {
            DrugInteractionAggregation aggregation = new DrugInteractionAggregation(
                    medicationA, medicationB);
            aggregation.setTotalInteractions(0);
            aggregation.setSevereInteractions(0);
            aggregation.setModerateInteractions(0);
            aggregation.setMildInteractions(0);
            aggregation.setOverallSeverity("NONE");
            aggregation.setInteractionCategories("");
            return aggregation;
        }

        // If there are existing aggregations, incorporate their counts
        if (aggregations != null && !aggregations.isEmpty()) {
            for (DrugInteractionAggregation existing : aggregations) {
                severeInteractionsCount += existing.getSevereInteractions();
                highCount += existing.getModerateInteractions();
                moderateCount += existing.getMildInteractions();
                mildCount += 0;
            }
        }

        // Perform therapeutic duplication check
        therapeuticDuplicationCheck(medicationA, medicationB, allInteractions);

        // Perform metabolic interaction check (CYP450 system)
        metabolicInteractionCheck(medicationA, medicationB, allInteractions);

        // Perform administration interaction check (route, timing, food interactions)
        administrationInteractionCheck(medicationA, medicationB, allInteractions);

        // Sort by severity (most severe first)
        allInteractions.sort(Comparator.comparing((DrugInteraction di) -> di.getSeverity())
                .reversed());

        // Count interactions by severity and categorize
        int total = allInteractions.size();
        int severeCount = (int) allInteractions.stream()
                .filter(d -> "CRITICAL".equals(d.getSeverity()))
                .count();
        int high = (int) allInteractions.stream()
                .filter(d -> "HIGH".equals(d.getSeverity()))
                .count();
        int moderate = (int) allInteractions.stream()
                .filter(d -> "MODERATE".equals(d.getSeverity()))
                .count();
        int mild = (int) allInteractions.stream()
                .filter(d -> "LOW".equals(d.getSeverity()))
                .count();

        // Add existing counts to new counts
        severeCount += severeInteractionsCount;
        high += highCount;
        moderate += moderateCount;
        mild += mildCount;

        // Determine overall severity
        String overallSeverity;
        if (severeCount > 0) {
            overallSeverity = "CRITICAL";
        } else if (high > 0) {
            overallSeverity = "HIGH";
        } else if (moderate > 0) {
            overallSeverity = "MODERATE";
        } else if (mild > 0) {
            overallSeverity = "LOW";
        } else {
            overallSeverity = "NONE";
        }

        // Collect interaction categories
        List<String> categories = allInteractions.stream()
                .map(DrugInteraction::getCategory)
                .distinct()
                .collect(Collectors.toList());

        DrugInteractionAggregation aggregation = new DrugInteractionAggregation(
                medicationA, medicationB);
        aggregation.setTotalInteractions(total);
        aggregation.setSevereInteractions(severeCount);
        aggregation.setModerateInteractions(high);
        aggregation.setMildInteractions(moderate);
        aggregation.setInteractionCategories(
                categories.isEmpty() ? "" : String.join(",", categories));
        aggregation.setOverallSeverity(overallSeverity);

        return aggregation;
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

    private boolean isAspirin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("aspirin") || name.contains("acetylsalicylic");
    }
}