package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionFollowup;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DrugInteractionFollowupService {

    /**
     * Check for required follow-up actions based on interaction type and severity.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param followups   existing follow-up records
     * @return updated/follow-up records with required actions
     */
    public List<DrugInteractionFollowup> checkFollowup(Medication medicationA,
                                                       Medication medicationB,
                                                       List<DrugInteractionFollowup> followups) {
        String followupType = determineFollowupType(medicationA, medicationB);
        String followupStatus = determineInitialStatus(followupType);

        DrugInteractionFollowup newFollowup = new DrugInteractionFollowup(
                medicationA, medicationB, LocalDateTime.now(), followupType, followupStatus);
        followups.add(newFollowup);

        return followups.stream()
                .peek(f -> applyRequiredAction(f, followupType))
                .collect(Collectors.toList());
    }

    private String determineFollowupType(Medication medicationA, Medication medicationB) {
        String nameA = (medicationA.getName() == null ? "" : medicationA.getName().toLowerCase());
        String nameB = (medicationB.getName() == null ? "" : medicationB.getName().toLowerCase());

        if (nameA.contains("warfarin") || nameB.contains("warfarin")) {
            return "CHECK_RESOLVE_REVIEW";
        }
        if (nameA.contains("insulin") || nameB.contains("insulin")) {
            return "medication review";
        }
        if (nameA.contains("anticoagulant") || nameB.contains("anticoagulant")) {
            return "dosage adjustment";
        }
        if (nameA.contains("antibiotic") || nameB.contains("antibiotic")) {
            return "monitoring plan";
        }
        return "specialist referral";
    }

    private String determineInitialStatus(String followupType) {
        if (followupType.equals("CHECK_RESOLVE_REVIEW")
                || followupType.equals("medication review")) {
            return "PENDING";
        }
        return "IN_PROGRESS";
    }

    private void applyRequiredAction(DrugInteractionFollowup followup, String followupType) {
        switch (followupType) {
            case "medication review":
                followup.setActionTaken("Review current medication regimen and adjust as needed");
                break;
            case "dosage adjustment":
                followup.setActionTaken("Adjust dosage based on interaction severity");
                break;
            case "monitoring plan":
                followup.setActionTaken("Establish monitoring plan with lab tests");
                break;
            case "specialist referral":
                followup.setActionTaken("Refer to appropriate specialist for evaluation");
                break;
            case "CHECK_RESOLVE_REVIEW":
                followup.setActionTaken("Review and resolve interaction findings");
                break;
            default:
                followup.setActionTaken("Monitor and document");
        }
    }
}