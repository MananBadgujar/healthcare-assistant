package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionResolution;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DrugInteractionResolutionService {

    /**
     * Resolve a drug-drug interaction between two medications.
     * <p>
     * Tracks the resolution status of the interaction and documents
     * the actions taken by the provider.
     *
     * @param medicationA first medication involved in the interaction
     * @param medicationB second medication involved in the interaction
     * @param resolutions existing resolution records to check against
     * @return the resolved DrugInteractionResolution record
     */
    public DrugInteractionResolution resolveInteraction(Medication medicationA,
                                                        Medication medicationB,
                                                        List<DrugInteractionResolution> resolutions) {
        DrugInteractionResolution resolution = new DrugInteractionResolution(
                medicationA, medicationB,
                "PENDING",
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );

        if (resolutions != null) {
            for (DrugInteractionResolution existing : resolutions) {
                if (existing.getMedicationA().getId().equals(medicationA.getId())
                        && existing.getMedicationB().getId().equals(medicationB.getId())) {
                    existing.setResolutionStatus("RESOLVED");
                    existing.setResolutionDate(LocalDateTime.now());
                    existing.setResolvedBy("PROVIDER");
                    existing.setActionTaken("Dosage adjustment and monitoring implemented");
                    existing.setResolutionCategory("DOSE_ADJUSTMENT");
                    existing.setProviderReviewed(true);
                    return existing;
                }
            }
        }

        return resolution;
    }
}