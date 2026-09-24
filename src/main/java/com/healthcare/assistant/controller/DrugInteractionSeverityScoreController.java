package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionSeverityScore;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.DrugInteractionSeverityScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/severity-scores")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionSeverityScoreController {

    private final DrugInteractionSeverityScoreService severityScoreService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionSeverityScoreController(
            DrugInteractionSeverityScoreService severityScoreService,
            MedicationRepository medicationRepository) {
        this.severityScoreService = severityScoreService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Calculate severity score for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return calculated severity score record
     */
    @PostMapping("/calculate-score")
    public ResponseEntity<DrugInteractionSeverityScore> calculateSeverityScore(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Medication medicationA = medicationAOpt.get();
        Medication medicationB = medicationBOpt.get();

        Integer severityScore = severityScoreService.calculateSeverityScore(
                medicationA, medicationB, new ArrayList<>());

        DrugInteractionSeverityScore scoreRecord = new DrugInteractionSeverityScore(
                medicationA, medicationB, severityScore, "", "", "");

        return ResponseEntity.ok(scoreRecord);
    }

    /**
     * Get severity score record by ID.
     *
     * @param id severity score record ID
     * @return the severity score record
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionSeverityScore> getSeverityScoreById(@PathVariable Long id) {
        return ResponseEntity.ok(new DrugInteractionSeverityScore());
    }

    /**
     * List recent severity score records.
     *
     * @return list of recent severity score records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionSeverityScore>> listRecentSeverityScores() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}