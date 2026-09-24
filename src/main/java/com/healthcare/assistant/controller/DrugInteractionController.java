package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteraction;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.healthcare.assistant.repository.MedicationRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cds/interactions")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionController {

    private final DrugInteractionService interactionService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionController(DrugInteractionService interactionService, MedicationRepository medicationRepository) {
        this.interactionService = interactionService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check for drug-drug interactions between two medications.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return detected interactions
     */
    @GetMapping
    public ResponseEntity<List<DrugInteraction>> checkInteraction(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DrugInteraction> interactions = interactionService.checkInteractions(
                medicationAOpt.get(), medicationBOpt.get());

        return ResponseEntity.ok(interactions);
    }

    /**
     * Check for drug-allergy interactions.
     *
     * @param medicationId ID of the medication
     * @param allergy      Description of the allergy
     * @return detected drug-allergy interactions
     */
    @GetMapping("/allergy")
    public ResponseEntity<List<DrugInteraction>> checkDrugAllergyInteraction(
            @RequestParam("medicationId") Long medicationId,
            @RequestParam("allergy") String allergy) {
        Optional<Medication> medicationOpt = medicationRepository.findById(medicationId);

        if (medicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DrugInteraction> interactions = interactionService.checkDrugAllergyInteraction(
                medicationOpt.get(), allergy);

        return ResponseEntity.ok(interactions);
    }

    /**
     * Get medication by ID.
     *
     * @param id medication ID
     * @return the medication details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Medication> getMedicationById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent interactions for audit.
     *
     * @return list of recent interactions
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteraction>> listRecentInteractions() {
        // In full implementation, fetch from repository with pagination
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    /**
     * Check interactions for a medication against multiple other medications.
     *
     * @param medicationId             ID of the medication to check
     * @param otherMedicationIds       IDs of other medications to check against
     * @return all detected interactions
     */
    @PostMapping("/check-multiple")
    public ResponseEntity<List<DrugInteraction>> checkInteractionsMultiple(
            @RequestParam("medicationId") Long medicationId,
            @RequestParam("otherMedicationIds") List<Long> otherMedicationIds) {
        Optional<Medication> medicationOpt = medicationRepository.findById(medicationId);

        if (medicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DrugInteraction> allInteractions = new java.util.ArrayList<>();
        for (Long otherId : otherMedicationIds) {
            Optional<Medication> otherOpt = medicationRepository.findById(otherId);
            if (otherOpt.isPresent()) {
                List<DrugInteraction> interactions = interactionService.checkInteractions(
                        medicationOpt.get(), otherOpt.get());
                allInteractions.addAll(interactions);
            }
        }

        return ResponseEntity.ok(allInteractions);
    }
}