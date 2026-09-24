package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.MaxDailyDose;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaxDailyDoseService {

    /**
     * Check if the current daily dose exceeds the maximum recommended dose for a medication.
     *
     * @param medication the medication to check
     * @param currentDailyDose the current daily dose being taken
     * @param exceedances list of MaxDailyDose records to check against
     * @return list of MaxDailyDose records where current dose exceeds max recommended dose
     */
    public List<MaxDailyDose> checkMaxDailyDose(Medication medication,
                                                 double currentDailyDose,
                                                 List<MaxDailyDose> exceedances) {
        List<MaxDailyDose> violations = new ArrayList<>();

        for (MaxDailyDose exceedance : exceedances) {
            if (exceedance.getMedication() == null || exceedance.getMedication().getId() == null) {
                continue;
            }
            if (exceedance.getMedication().getId().equals(medication.getId())) {
                double maxDose = Double.parseDouble(exceedance.getMaxDailyDose());
                if (currentDailyDose > maxDose) {
                    exceedance.setCurrentDailyDose(String.valueOf(currentDailyDose));
                    exceedance.setSeverity(calculateSeverity(currentDailyDose, exceedance.getMaxDailyDose()));
                    exceedance.setUpdatedAt(LocalDateTime.now());
                    violations.add(exceedance);
                }
            }
        }

        return violations;
    }

    /**
     * Calculate severity based on how much the current dose exceeds the maximum.
     */
    private String calculateSeverity(double currentDailyDose, String maxDailyDose) {
        double maxDose = Double.parseDouble(maxDailyDose);
        double ratio = currentDailyDose / maxDose;

        if (ratio >= 2.0) {
            return "CRITICAL";
        } else if (ratio >= 1.5) {
            return "HIGH";
        } else if (ratio >= 1.2) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }
}