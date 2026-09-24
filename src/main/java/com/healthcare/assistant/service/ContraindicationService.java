package com.healthcare.assistant.service;

import org.springframework.stereotype.Service;

import com.healthcare.assistant.entity.Contraindication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.enums.ContraindicationSeverity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for detecting clinical contraindications.
 * <p>
 * This service analyzes patient context (conditions, medications, allergies,
 * labs, vitals) against established clinical rules to identify contraindications.
 * AI-assisted recommendations must pass through deterministic safety validation
 * before reaching the provider.
 */
@Service
public class ContraindicationService {

    /**
     * Detect contraindications for a patient given their current context.
     *
     * @param patient the patient to analyze
     * @param medications current medications
     * @param conditionNames existing medical condition names
     * @param allergies allergy information
     * @return list of detected contraindications
     */
    public List<Contraindication> detectContraindications(
            Patient patient,
            List<Medication> medications,
            List<String> conditionNames,
            String allergies) {
        List<Contraindication> contraindications = new ArrayList<>();

        if (patient == null) {
            return contraindications;
        }

        // Check medication-condition contraindications
        medicationConditionContraindications(patient, medications, conditionNames, contraindications);

        // Check medication-allergy contraindications
        medicationAllergyContraindications(patient, medications, allergies, contraindications);

        // Sort by severity (most severe first)
        contraindications.sort((a, b) -> severityCompare(b.getSeverity().name(), a.getSeverity().name()));

        return contraindications;
    }

    /**
     * Check for contraindications between medications and medical conditions.
     */
    private void medicationConditionContraindications(
            Patient patient,
            List<Medication> medications,
            List<String> conditionNames,
            List<Contraindication> contraindications) {
        if (conditionNames == null) {
            return;
        }

        for (String conditionName : conditionNames) {
            String lowerCondition = conditionName != null ? conditionName.toLowerCase() : "";

            for (Medication medication : medications) {
                if (medication.getName() == null) {
                    continue;
                }
                String medName = medication.getName().toLowerCase();

                // Example: ACE inhibitors in angioedema history
                if (isAceInhibitor(medName) && isAngioEdema(lowerCondition)) {
                    Contraindication contra = new Contraindication(
                            patient, medication.getName(), lowerCondition,
                            ContraindicationSeverity.CRITICAL,
                            "ACE inhibitor contraindicated in history of angioedema.",
                            "Avoid ACE inhibitors; consider alternative antihypertensive therapy.",
                            "Clinical guideline: ACE inhibitors and angioedema"
                    );
                    contraindications.add(contra);
                }

                // Example: Beta-blockers in asthma
                if (isBetaBlocker(medName) && isAsthma(lowerCondition)) {
                    Contraindication contra = new Contraindication(
                            patient, medication.getName(), lowerCondition,
                            ContraindicationSeverity.HIGH,
                            "Beta-blocker contraindicated in asthma.",
                            "Avoid non-selective beta-blockers; consider cardioselective beta-blocker if essential.",
                            "Clinical guideline: Beta-blockers and asthma"
                    );
                    contraindications.add(contra);
                }

                // Example: Warfarin in pregnancy
                if (isWarfarin(medName) && isPregnancy(lowerCondition)) {
                    Contraindication contra = new Contraindication(
                            patient, medication.getName(), lowerCondition,
                            ContraindicationSeverity.CRITICAL,
                            "Warfarin contraindicated in pregnancy - risk of fetal abnormalities.",
                            "Use alternative anticoagulation; discuss with OB-GYN.",
                            "Clinical guideline: Warfarin and pregnancy"
                    );
                    contraindications.add(contra);
                }

                // Example: Aspirin in bleeding disorder
                if (isAspirin(medName) && isBleedingDisorder(lowerCondition)) {
                    Contraindication contra = new Contraindication(
                            patient, medication.getName(), lowerCondition,
                            ContraindicationSeverity.HIGH,
                            "Aspirin contraindicated in bleeding disorder.",
                            "Avoid aspirin; use alternative antiplatelet or anticoagulant.",
                            "Clinical guideline: Aspirin and bleeding disorders"
                    );
                    contraindications.add(contra);
                }
            }
        }
    }

    /**
     * Check for contraindications between medications and allergies.

    /**
     * Check for contraindications between medications and allergies.
     */
    private void medicationAllergyContraindications(
            Patient patient,
            List<Medication> medications,
            String allergies,
            List<Contraindication> contraindications) {
        if (allergies == null || allergies.isBlank()) {
            return;
        }

        String allergyLower = allergies.toLowerCase();

        for (Medication medication : medications) {
            if (medication.getName() == null) {
                continue;
            }
            String medName = medication.getName().toLowerCase();

            // Example: Penicillin in penicillin allergy
            if (isPenicillinAllergy(allergyLower) && isPenicillin(medName)) {
                Contraindication contra = new Contraindication(
                        patient, medication.getName(), null,
                        ContraindicationSeverity.CRITICAL,
                        "Penicillin contraindicated - patient has penicillin allergy.",
                        "Avoid penicillin and related antibiotics; use alternative antibiotic.",
                        "Clinical guideline: Penicillin allergy and penicillin antibiotics"
                );
                contraindications.add(contra);
            }

            // Example: Shellfish contrast in shellfish allergy
            if (isShellfishAllergy(allergyLower) && isIodinatedContrast(medName)) {
                Contraindication contra = new Contraindication(
                        patient, medication.getName(), null,
                        ContraindicationSeverity.MODERATE,
                        "Iodinated contrast contraindicated with shellfish allergy.",
                        "Pre-medicate with steroids and antihistamines; consider non-iodinated contrast.",
                        "Clinical guideline: Shellfish allergy and iodinated contrast"
                );
                contraindications.add(contra);
            }
        }
    }

    /**
     * Compare severity strings for sorting.
     */
    private int severityCompare(String a, String b) {
        String[] order = {"CRITICAL", "HIGH", "MODERATE", "LOW", "NONE"};
        int idxA = -1, idxB = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(a)) idxA = i;
            if (order[i].equals(b)) idxB = i;
        }
        return Integer.compare(idxB, idxA); // descending order
    }

    // Helper methods for clinical rule detection

    private boolean isAceInhibitor(String medName) {
        return medName.contains("ace") || medName.contains("captopril") ||
                medName.contains("enalapril") || medName.contains("lisinopril") ||
                medName.contains("perindopril") || medName.contains("ramipril");
    }

    private boolean isAngioEdema(String conditionName) {
        return conditionName.contains("angioedema") || conditionName.contains("angio oedema");
    }

    private boolean isBetaBlocker(String medName) {
        return medName.contains("beta") || medName.contains("metoprolol") ||
                medName.contains("atenolol") || medName.contains("propranolol") ||
                medName.contains("carvedilol") || medName.contains("bisoprolol");
    }

    private boolean isAsthma(String conditionName) {
        return conditionName.contains("asthma") || conditionName.contains("bronchial asthma");
    }

    private boolean isWarfarin(String medName) {
        return medName.contains("warfarin");
    }

    private boolean isPregnancy(String conditionName) {
        return conditionName.contains("pregnancy") || conditionName.contains("gestational");
    }

    private boolean isBetaBlockerAsthmaContraindication(String medName, String conditionName) {
        return isBetaBlocker(medName) && isAsthma(conditionName);
    }

    private boolean isAspirin(String medName) {
        return medName.contains("aspirin") || medName.contains("acetylsalicylic");
    }

    private boolean isBleedingDisorder(String conditionName) {
        return conditionName.contains("bleeding") || conditionName.contains("hemophilia")
                || conditionName.contains("coagulation disorder");
    }

    private boolean isPenicillinAllergy(String allergy) {
        return allergy.contains("penicillin") || allergy.contains("penicillins") ||
                allergy.contains("penam") || allergy.contains("penicillin allergy");
    }

    private boolean isPenicillin(String medName) {
        String lower = medName.toLowerCase();
        return lower.contains("penicillin") || lower.contains("penam")
                || lower.contains("penicillin") || lower.contains("benzathine");
    }

    private boolean isShellfishAllergy(String allergy) {
        return allergy.contains("shellfish") || allergy.contains("shrimp")
                || allergy.contains("crab") || allergy.contains("mollusk");
    }

    private boolean isIodinatedContrast(String medName) {
        String lower = medName.toLowerCase();
        return lower.contains("contrast") || lower.contains("iohexol")
                || lower.contains("iopamidol") || lower.contains("iodine");
    }
}