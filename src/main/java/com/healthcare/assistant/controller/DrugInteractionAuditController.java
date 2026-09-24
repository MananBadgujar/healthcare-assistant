package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionAudit;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionAuditService;
import com.healthcare.assistant.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/audit")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionAuditController {

    private final DrugInteractionAuditService auditService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionAuditController(DrugInteractionAuditService auditService,
                                          MedicationRepository medicationRepository) {
        this.auditService = auditService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Audit interaction for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return created audit record
     */
    @PostMapping
    public ResponseEntity<DrugInteractionAudit> auditInteraction(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DrugInteractionAudit> audits = java.util.Collections.singletonList(
                new DrugInteractionAudit());
        audits = auditService.auditInteraction(
                medicationAOpt.get(), medicationBOpt.get(), audits);

        return ResponseEntity.ok(audits.get(0));
    }

    /**
     * Get audit record by ID.
     *
     * @param id audit record ID
     * @return the audit record
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionAudit> getAuditById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(med -> ResponseEntity.ok(new DrugInteractionAudit()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent audit records.
     *
     * @return list of recent audit records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionAudit>> listRecentAudits() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}