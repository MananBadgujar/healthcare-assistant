package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionContraindication;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.enums.ContraindicationSeverity;
import com.healthcare.assistant.entity.enums.ContraindicationType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DrugInteractionContraindicationService {

    public DrugInteractionContraindication checkContraindication(
            Medication medicationA, Medication medicationB,
            List<DrugInteractionContraindication> contraindications) {

        String medAName = medicationA != null ? medicationA.getName().toLowerCase() : "";
        String medBName = medicationB != null ? medicationB.getName().toLowerCase() : "";

        // Check drug-allergy cross-reactivity: penicillin cross-reactivity
        if (isPenicillin(medAName) && containsAllergyKeyword(medBName, "penicillin")) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_ALLERGY,
                    ContraindicationSeverity.CRITICAL,
                    "Penicillin cross-reactivity detected.",
                    "Avoid penicillin and related antibiotics; use alternative antibiotic.",
                    "Clinical guideline: Penicillin cross-reactivity",
                    "Drug-allergy"
            );
            contraindications.add(contra);
        }

        if (isPenicillin(medBName) && containsAllergyKeyword(medAName, "penicillin")) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_ALLERGY,
                    ContraindicationSeverity.CRITICAL,
                    "Penicillin cross-reactivity detected.",
                    "Avoid penicillin and related antibiotics; use alternative antibiotic.",
                    "Clinical guideline: Penicillin cross-reactivity",
                    "Drug-allergy"
            );
            contraindications.add(contra);
        }

        // Check drug-condition contraindications: ACE inhibitor + angioedema
        if (isAceInhibitor(medAName) && isAngioEdema(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.CRITICAL,
                    "ACE inhibitor contraindicated with angioedema.",
                    "Avoid ACE inhibitors; consider alternative antihypertensive therapy.",
                    "Clinical guideline: ACE inhibitors and angioedema",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isBetaBlocker(medAName) && isAsthma(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.HIGH,
                    "Beta-blocker contraindicated with asthma.",
                    "Avoid non-selective beta-blockers; consider cardioselective beta-blocker if essential.",
                    "Clinical guideline: Beta-blockers and asthma",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isWarfarin(medAName) && isPregnancy(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.CRITICAL,
                    "Warfarin contraindicated in pregnancy - risk of fetal abnormalities.",
                    "Use alternative anticoagulation; discuss with OB-GYN.",
                    "Clinical guideline: Warfarin and pregnancy",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isAspirin(medAName) && isBleedingDisorder(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.HIGH,
                    "Aspirin contraindicated with bleeding disorder.",
                    "Avoid aspirin; use alternative antiplatelet or anticoagulant.",
                    "Clinical guideline: Aspirin and bleeding disorders",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        // Check drug-condition contraindications: reverse order
        if (isAceInhibitor(medBName) && isAngioEdema(medAName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.CRITICAL,
                    "ACE inhibitor contraindicated with angioedema.",
                    "Avoid ACE inhibitors; consider alternative antihypertensive therapy.",
                    "Clinical guideline: ACE inhibitors and angioedema",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isBetaBlocker(medBName) && isAsthma(medAName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.HIGH,
                    "Beta-blocker contraindicated with asthma.",
                    "Avoid non-selective beta-blockers; consider cardioselective beta-blocker if essential.",
                    "Clinical guideline: Beta-blockers and asthma",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isWarfarin(medBName) && isPregnancy(medAName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.CRITICAL,
                    "Warfarin contraindicated in pregnancy - risk of fetal abnormalities.",
                    "Use alternative anticoagulation; discuss with OB-GYN.",
                    "Clinical guideline: Warfarin and pregnancy",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isAspirin(medBName) && isBleedingDisorder(medAName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_CONDITION,
                    ContraindicationSeverity.HIGH,
                    "Aspirin contraindicated with bleeding disorder.",
                    "Avoid aspirin; use alternative antiplatelet or anticoagulant.",
                    "Clinical guideline: Aspirin and bleeding disorders",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        // Check drug-drug contraindications
        if (isWarfarin(medAName) && isAnticoagulant(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_DRUG,
                    ContraindicationSeverity.CRITICAL,
                    "Concurrent anticoagulant therapy increases bleeding risk.",
                    "Monitor coagulation parameters; adjust anticoagulant doses.",
                    "Clinical guideline: Warfarin and concurrent anticoagulants",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isAspirin(medAName) && isAnticoagulant(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_DRUG,
                    ContraindicationSeverity.HIGH,
                    "Aspirin plus anticoagulant increases bleeding risk.",
                    "Monitor for signs of bleeding; adjust doses as needed.",
                    "Clinical guideline: Aspirin and anticoagulant interaction",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        if (isAceInhibitor(medAName) && isPotassiumSupplement(medBName)) {
            DrugInteractionContraindication contra = new DrugInteractionContraindication(
                    medicationA, medicationB,
                    ContraindicationType.DRUG_DRUG,
                    ContraindicationSeverity.HIGH,
                    "ACE inhibitor plus potassium supplement risk of hyperkalemia.",
                    "Monitor potassium levels; consider potassium-sparing diuretic alternative.",
                    "Clinical guideline: ACE inhibitors and potassium supplementation",
                    " drug-interaction"
            );
            contraindications.add(contra);
        }

        return new DrugInteractionContraindication(medicationA, medicationB,
                ContraindicationType.DRUG_DRUG, ContraindicationSeverity.NONE, "", "", "", " drug-interaction");
    }

    private boolean containsAllergyKeyword(String medName, String keyword) {
        return medName != null && medName.toLowerCase().contains(keyword.toLowerCase());
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

    private boolean isAspirin(String medName) {
        return medName.contains("aspirin") || medName.contains("acetylsalicylic");
    }

    private boolean isBleedingDisorder(String conditionName) {
        return conditionName.contains("bleeding") || conditionName.contains("hemophilia")
                || conditionName.contains("coagulation disorder");
    }

    private boolean isAnticoagulant(String medName) {
        String lower = medName.toLowerCase();
        return lower.contains("warfarin") || lower.contains("heparin")
                || lower.contains("enoxaparin") || lower.contains("dalteparin")
                || lower.contains("nadroparin") || lower.contains("tinzaparin")
                || lower.contains("apixaban") || lower.contains("rivaroxaban")
                || lower.contains("dabigatran") || lower.contains("edoxaban");
    }

    private boolean isPotassiumSupplement(String medName) {
        String lower = medName.toLowerCase();
        return lower.contains("potassium") || lower.contains("k-slang")
                || lower.contains("klor-con") || lower.contains("bicarbonate");
    }

    private boolean isPenicillin(String medName) {
        String lower = medName.toLowerCase();
        return lower.contains("penicillin") || lower.contains("penam")
                || lower.contains("benzathine") || lower.contains("pen vk");
    }
}