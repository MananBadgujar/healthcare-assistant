package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionRisk;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DrugInteractionRiskService {

    /**
     * Assess the overall risk level between two medications based on detected interactions.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param risks       list of existing drug interaction risk records
     * @return the overall assessed risk level
     */
    public DrugInteractionRisk.RiskLevel assessRisk(Medication medicationA,
                                                   Medication medicationB,
                                                   List<DrugInteractionRisk> risks) {
        if (risks == null || risks.isEmpty()) {
            return DrugInteractionRisk.RiskLevel.LOW;
        }

        long criticalCount = risks.stream()
                .filter(r -> DrugInteractionRisk.RiskLevel.CRITICAL.equals(r.getRiskLevel()))
                .count();

        long highCount = risks.stream()
                .filter(r -> DrugInteractionRisk.RiskLevel.HIGH.equals(r.getRiskLevel()))
                .count();

        long moderateCount = risks.stream()
                .filter(r -> DrugInteractionRisk.RiskLevel.MODERATE.equals(r.getRiskLevel()))
                .count();

        if (criticalCount > 0) {
            return DrugInteractionRisk.RiskLevel.CRITICAL;
        } else if (highCount > 0) {
            return DrugInteractionRisk.RiskLevel.HIGH;
        } else if (moderateCount >= 3) {
            return DrugInteractionRisk.RiskLevel.MODERATE;
        } else if (moderateCount > 0) {
            return DrugInteractionRisk.RiskLevel.MODERATE;
        } else {
            return DrugInteractionRisk.RiskLevel.LOW;
        }
    }
}