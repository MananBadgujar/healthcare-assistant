package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionFollowup;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionFollowupService;
import com.healthcare.assistant.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cds/followup")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionFollowupController {

    private final DrugInteractionFollowupService followupService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionFollowupController(DrugInteractionFollowupService followupService,
                                             MedicationRepository medicationRepository) {
        this.followupService = followupService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check for required follow-up actions for two medications.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return created/updated follow-up record
     */
    @PostMapping("/check-followup")
    public ResponseEntity<DrugInteractionFollowup> checkFollowup(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DrugInteractionFollowup> followups = new java.util.ArrayList<>();
        followupService.checkFollowup(
                medicationAOpt.get(), medicationBOpt.get(), followups);

        if (!followups.isEmpty()) {
            return ResponseEntity.ok(followups.get(0));
        }

        return ResponseEntity.ok(new DrugInteractionFollowup());
    }

    /**
     * Get follow-up record by ID.
     *
     * @param id follow-up record ID
     * @return the follow-up record
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionFollowup> getFollowupById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(med -> ResponseEntity.ok(new DrugInteractionFollowup()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent follow-up records.
     *
     * @return list of recent follow-up records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionFollowup>> listRecentFollowups() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}