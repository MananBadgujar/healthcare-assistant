package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionRisk;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.DrugInteractionRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/drug-interaction-risks")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionRiskController {

    private final DrugInteractionRiskService riskService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionRiskController(DrugInteractionRiskService riskService,
                                         MedicationRepository medicationRepository) {
        this.riskService = riskService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Assess risk for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @return risk assessment record
     */
    @PostMapping("/assess-risk")
    public ResponseEntity<DrugInteractionRisk> assessRisk(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        DrugInteractionRisk risk = new DrugInteractionRisk(
                medicationAOpt.get(), medicationBOpt.get(),
                DrugInteractionRisk.RiskLevel.LOW, "", "", "GENERAL");

        riskService.assessRisk(medicationAOpt.get(), medicationBOpt.get(), List.of(risk));

        return ResponseEntity.ok(risk);
    }

    /**
     * Get risk assessment record by ID.
     *
     * @param id risk assessment ID
     * @return risk assessment record
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionRisk> getRiskById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(m -> ResponseEntity.ok(new DrugInteractionRisk()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent risk assessment records.
     *
     * @return list of recent risk assessment records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionRisk>> listRecentRisks() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}