package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugAllergyCrossReactivity;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.DrugAllergyCrossReactivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/drug-allergy-cross-reactivities")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugAllergyCrossReactivityController {

    private final DrugAllergyCrossReactivityService crossReactivityService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugAllergyCrossReactivityController(
            DrugAllergyCrossReactivityService crossReactivityService,
            MedicationRepository medicationRepository) {
        this.crossReactivityService = crossReactivityService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check cross-reactivity for a medication and allergy by ID.
     *
     * @param medicationId       ID of the medication
     * @param allergy            description of the allergy
     * @param crossReactivityId ID of the cross-reactivity record to check against
     * @return detected cross-reactivity records
     */
    @PostMapping("/check-cross-reactivity/{crossReactivityId}")
    public ResponseEntity<List<DrugAllergyCrossReactivity>> checkCrossReactivity(
            @PathVariable("crossReactivityId") Long crossReactivityId,
            @RequestParam("medicationId") Long medicationId,
            @RequestParam("allergy") String allergy) {
        Optional<Medication> medicationOpt = medicationRepository.findById(medicationId);

        if (medicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Load known cross-reactivity records (in full implementation, fetch from repository)
        List<DrugAllergyCrossReactivity> crossReactivities = java.util.Collections.emptyList();

        List<DrugAllergyCrossReactivity> results = crossReactivityService.checkCrossReactivity(
                medicationOpt.get(), allergy, crossReactivities);

        return ResponseEntity.ok(results);
    }

    /**
     * Get cross-reactivity record by ID.
     *
     * @param id cross-reactivity record ID
     * @return the cross-reactivity record details
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugAllergyCrossReactivity> getCrossReactivityById(@PathVariable Long id) {
        return ResponseEntity.ok(new DrugAllergyCrossReactivity());
    }

    /**
     * List recent cross-reactivity records for audit.
     *
     * @return list of recent cross-reactivity records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugAllergyCrossReactivity>> listRecentCrossReactivities() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}