package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionAudit;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DrugInteractionAuditService {

    /**
     * Audit a drug interaction between two medications.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param audits      existing audit records
     * @return updated audit records
     */
    public List<DrugInteractionAudit> auditInteraction(Medication medicationA,
                                                         Medication medicationB,
                                                         List<DrugInteractionAudit> audits) {
        DrugInteractionAudit newAudit = new DrugInteractionAudit(
                medicationA, medicationB, "INTERACTION_CHECK", "Interaction audit completed",
                "COMPLIANT", false);

        audits.add(newAudit);

        return audits.stream()
                .peek(a -> applyRequiredReview(a, "INTERACTION_CHECK"))
                .collect(java.util.stream.Collectors.toList());
    }

    private void applyRequiredReview(DrugInteractionAudit audit, String auditType) {
        if (auditType.equals("INTERACTION_CHECK")) {
            audit.setAuditFindings("Review interaction findings; ensure appropriate monitoring and documentation.");
        }
    }
}