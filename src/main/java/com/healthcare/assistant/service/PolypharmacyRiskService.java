package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.PolypharmacyRisk;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolypharmacyRiskService {

    public PolypharmacyRisk checkPolypharmacyRisk(List<Medication> medications,
                                                  List<PolypharmacyRisk> risks) {
        int medicationCount = medications.size();

        int severeCount = 0;
        int moderateCount = 0;
        int mildCount = 0;

        if (risks != null) {
            for (PolypharmacyRisk risk : risks) {
                if (risk.getSevereInteractionCount() > 0) {
                    severeCount += risk.getSevereInteractionCount();
                }
                if (risk.getModerateInteractionCount() > 0) {
                    moderateCount += risk.getModerateInteractionCount();
                }
                if (risk.getMildInteractionCount() > 0) {
                    mildCount += risk.getMildInteractionCount();
                }
            }
        }

        PolypharmacyRisk.RiskLevel riskLevel;
        if (medicationCount >= 5) {
            riskLevel = PolypharmacyRisk.RiskLevel.HIGH;
        } else if (medicationCount >= 3) {
            riskLevel = PolypharmacyRisk.RiskLevel.MODERATE;
        } else if (medicationCount >= 1) {
            riskLevel = PolypharmacyRisk.RiskLevel.LOW;
        } else {
            riskLevel = PolypharmacyRisk.RiskLevel.LOW;
        }

        int totalInteractions = severeCount + moderateCount + mildCount;
        String riskFactors = String.format(
                "Medications: %d, Severe interactions: %d, Moderate interactions: %d, Mild interactions: %d",
                medicationCount, severeCount, moderateCount, mildCount);

        String clinicalRecommendations;
        if (riskLevel == PolypharmacyRisk.RiskLevel.CRITICAL || riskLevel == PolypharmacyRisk.RiskLevel.HIGH) {
            clinicalRecommendations = "Provider review required. Consider medication review and reduction. Monitor for adverse events.";
        } else if (riskLevel == PolypharmacyRisk.RiskLevel.MODERATE) {
            clinicalRecommendations = "Review medication regimen. Monitor for interactions. Consider dose adjustments or alternatives.";
        } else {
            clinicalRecommendations = "Continue current regimen with routine monitoring. Educate patient on potential interactions.";
        }

        PolypharmacyRisk risk = new PolypharmacyRisk(
                null, medicationCount, severeCount, moderateCount, mildCount, riskLevel);
        risk.setRiskFactors(riskFactors);
        risk.setClinicalRecommendations(clinicalRecommendations);
        risk.setDetectedAt(LocalDateTime.now());
        risk.setUpdatedAt(LocalDateTime.now());

        return risk;
    }
}