package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugAllergyCrossReactivity;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DrugAllergyCrossReactivityService {

    private final MedicationRepository medicationRepository;

    public DrugAllergyCrossReactivityService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check for known drug-allergy cross-reactivity.
     *
     * @param medication    the medication to check
     * @param allergy       the allergy to check against
     * @param crossReactivities list of known cross-reactivity records
     * @return list of detected cross-reactivity records
     */
    public List<DrugAllergyCrossReactivity> checkCrossReactivity(
            Medication medication, String allergy,
            List<DrugAllergyCrossReactivity> crossReactivities) {
        List<DrugAllergyCrossReactivity> results = new ArrayList<>();

        if (medication == null || allergy == null || allergy.isBlank()) {
            return results;
        }

        String allergyLower = allergy.toLowerCase();

        for (DrugAllergyCrossReactivity record : crossReactivities) {
            if (isCrossReactive(record, allergyLower, medication)) {
                record.setUpdatedAt(LocalDateTime.now());
                results.add(record);
            }
        }

        return results;
    }

    private boolean isCrossReactive(DrugAllergyCrossReactivity record, String allergyLower, Medication medication) {
        String triggeringAllergy = (record.getTriggeringAllergy() == null ? "" : record.getTriggeringAllergy().toLowerCase());
        String category = record.getCategory();

        if (triggeringAllergy.contains("penicillin") || triggeringAllergy.contains("penicillins")) {
            if (isCephalosporin(medication) || isPenicillin(medication)) {
                return true;
            }
        }

        if (triggeringAllergy.contains("shellfish") || triggeringAllergy.contains("shrimp")
                || triggeringAllergy.contains("crab") || triggeringAllergy.contains("mollusk")) {
            if (isIodinatedContrast(medication)) {
                return true;
            }
        }

        if (category != null && category.contains("iodinated-contrast")
                && isIodinatedContrast(medication)) {
            return true;
        }

        return false;
    }

    // Helper methods for clinical rule detection

    private boolean isPenicillinAllergy(String allergy) {
        return allergy.contains("penicillin") || allergy.contains("penicillins")
                || allergy.contains("penam") || allergy.contains("penicillin allergy");
    }

    private boolean isCephalosporin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("cef") || name.contains("cefaclor")
                || name.contains("cefpodoxime") || name.contains("ceftriaxone")
                || name.contains("cefuroxime");
    }

    private boolean isShellfishAllergy(String allergy) {
        return allergy.contains("shellfish") || allergy.contains("shrimp")
                || allergy.contains("crab") || allergy.contains("mollusk");
    }

    private boolean isIodinatedContrast(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("contrast") || name.contains("iohexol")
                || name.contains("iopamidol") || name.contains("iodine");
    }

    private boolean isPenicillin(Medication med) {
        String name = (med.getName() == null ? "" : med.getName().toLowerCase());
        return name.contains("penicillin") || name.contains("penam")
                || name.contains("benzathine");
    }
}