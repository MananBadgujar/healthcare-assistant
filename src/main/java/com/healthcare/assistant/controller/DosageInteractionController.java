package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DosageInteraction;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DosageInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.healthcare.assistant.repository.MedicationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/dosage-interactions")
@PreAuthorize("hasRole('PROVIDER')")
public class DosageInteractionController {

    private final DosageInteractionService dosageInteractionService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DosageInteractionController(DosageInteractionService dosageInteractionService,
                                       MedicationRepository medicationRepository) {
        this.dosageInteractionService = dosageInteractionService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check for dosage interactions between two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return detected dosage interactions
     */
    @PostMapping("/check-dosage")
    public ResponseEntity<List<DosageInteraction>> checkDosageInteraction(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DosageInteraction> interactions = new ArrayList<>();
        dosageInteractionService.dosageInteractionCheck(
                medicationAOpt.get(), medicationBOpt.get(), interactions);

        return ResponseEntity.ok(interactions);
    }

    /**
     * Get dosage interaction by ID.
     *
     * @param id dosage interaction ID
     * @return the dosage interaction details
     */
    @GetMapping("/{id}")
    public ResponseEntity<DosageInteraction> getDosageInteractionById(@PathVariable Long id) {
        // Since there's no DosageInteractionRepository, this endpoint
        // would typically fetch from the database. For now, return not found.
        return ResponseEntity.notFound().build();
    }

    /**
     * List recent dosage interactions for audit.
     *
     * @return list of recent dosage interactions
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DosageInteraction>> listRecentDosageInteractions() {
        // In full implementation, fetch from repository with pagination
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}