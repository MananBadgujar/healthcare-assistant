package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionAggregation;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.healthcare.assistant.repository.MedicationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/interaction-aggregations")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionAggregationController {

    private final DrugInteractionAggregationService aggregationService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionAggregationController(
            DrugInteractionAggregationService aggregationService,
            MedicationRepository medicationRepository) {
        this.aggregationService = aggregationService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Aggregate interactions for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return aggregated interaction record
     */
    @PostMapping("/aggregate")
    public ResponseEntity<DrugInteractionAggregation> aggregateInteractions(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        DrugInteractionAggregation aggregation = aggregationService.aggregateInteractions(
                medicationAOpt.get(), medicationBOpt.get(), new ArrayList<>());

        return ResponseEntity.ok(aggregation);
    }

    /**
     * Get aggregation record by ID.
     *
     * @param id aggregation record ID
     * @return the aggregation record
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionAggregation> getAggregationById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(m -> ResponseEntity.ok(new DrugInteractionAggregation()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent aggregation records.
     *
     * @return list of recent aggregation records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionAggregation>> listRecentAggregations() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}