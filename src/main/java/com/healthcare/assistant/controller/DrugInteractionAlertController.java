package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionAlert;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.DrugInteractionAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/drug-interaction-alerts")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionAlertController {

    private final DrugInteractionAlertService drugInteractionAlertService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionAlertController(DrugInteractionAlertService drugInteractionAlertService,
                                          MedicationRepository medicationRepository) {
        this.drugInteractionAlertService = drugInteractionAlertService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Prioritize alerts for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @param alerts list of drug interaction alerts
     * @return prioritized alert record
     */
    @PostMapping("/prioritize")
    public ResponseEntity<DrugInteractionAlert> prioritizeAlerts(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId,
            @RequestBody List<DrugInteractionAlert> alerts) {
        Optional<Medication> medAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medBOpt = medicationRepository.findById(medicationBId);

        if (medAOpt.isEmpty() || medBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Medication medicationA = medAOpt.get();
        Medication medicationB = medBOpt.get();

        return ResponseEntity.ok(drugInteractionAlertService.prioritizeAlerts(
                medicationA, medicationB, alerts));
    }

    /**
     * Get alert record by ID.
     *
     * @param id alert record ID
     * @return alert record or not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionAlert> getAlertById(@PathVariable Long id) {
        Optional<DrugInteractionAlert> alertOpt = findAlertById(id);
        return alertOpt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    private Optional<DrugInteractionAlert> findAlertById(Long id) {
        // In a full implementation, this would use a DrugInteractionAlertRepository
        // For now, return empty to simulate not found
        return Optional.empty();
    }

    /**
     * List recent alert records.
     *
     * @return list of recent alert records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionAlert>> listRecentAlerts() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}