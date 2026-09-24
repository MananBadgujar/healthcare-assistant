package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionConsent;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DrugInteractionConsentService {

    /**
     * Check whether proper consent has been obtained for the drug interaction.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param consents    list of consent records to check against
     * @return true if proper consent is documented, false otherwise
     */
    public boolean checkConsent(Medication medicationA, Medication medicationB,
                                List<DrugInteractionConsent> consents) {
        if (consents == null || consents.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        for (DrugInteractionConsent consent : consents) {
            if (consent.getMedicationA().getId() != null
                    && consent.getMedicationA().getId().equals(medicationA.getId())
                    && consent.getMedicationB().getId() != null
                    && consent.getMedicationB().getId().equals(medicationB.getId())) {
                if (consent.getConsentGiven() != null && consent.getConsentGiven()
                        && consent.getConsentDate() != null
                        && consent.getConsentBy() != null
                        && !consent.getConsentBy().isBlank()
                        && consent.getConsentScope() != null
                        && (!consent.getConsentScope().equals("NONE")
                        || consent.getSideEffectsDiscussed() != null
                        && consent.getSideEffectsDiscussed()
                        || consent.getMonitoringPlan() != null
                        && !consent.getMonitoringPlan().isBlank())) {
                    return true;
                }
            }
        }

        return false;
    }
}