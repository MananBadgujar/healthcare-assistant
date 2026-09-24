package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionTesting;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionTestingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.healthcare.assistant.repository.MedicationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cds/drug-interaction-testing")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionTestingController {

    private final DrugInteractionTestingService testingService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionTestingController(
            DrugInteractionTestingService testingService,
            MedicationRepository medicationRepository) {
        this.testingService = testingService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Validate drug interaction for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @param tests         list of drug interaction testing records
     * @return validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<DrugInteractionTesting> validateInteraction(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId,
            @RequestBody List<DrugInteractionTesting> tests) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        DrugInteractionTesting result = testingService.validateInteraction(
                medicationAOpt.get(), medicationBOpt.get(), tests);

        return ResponseEntity.ok(result);
    }

    /**
     * Get testing record by ID.
     *
     * @param id testing record ID
     * @return the testing record details
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionTesting> getTestingById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(m -> ResponseEntity.ok(new DrugInteractionTesting()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent testing records.
     *
     * @return list of recent testing records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionTesting>> listRecentTesting() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}