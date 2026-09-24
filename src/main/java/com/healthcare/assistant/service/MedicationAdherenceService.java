package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.MedicationAdherence;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MedicationAdherenceService {

    public double checkAdherence(Medication medication, LocalDateTime lastTaken, List<MedicationAdherence> adherenceRecords) {
        if (lastTaken == null) {
            return 0.0;
        }

        long daysSinceLastTaken = ChronoUnit.DAYS.between(lastTaken, LocalDateTime.now());
        if (daysSinceLastTaken > 30) {
            return 0.0;
        }

        int totalDoses = 30;
        int missed = 0;
        for (MedicationAdherence record : adherenceRecords) {
            missed += record.getMissedDoses();
        }

        double adherence = ((totalDoses - missed) / (double) totalDoses) * 100;
        return Math.max(0, Math.min(100, adherence));
    }
}