package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.PolypharmacyRisk;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.PolypharmacyRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cds/polypharmacy-risks")
@PreAuthorize("hasRole('PROVIDER')")
public class PolypharmacyRiskController {

    private final PolypharmacyRiskService polypharmacyRiskService;
    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public PolypharmacyRiskController(PolypharmacyRiskService polypharmacyRiskService,
                                       MedicationRepository medicationRepository,
                                       PatientRepository patientRepository) {
        this.polypharmacyRiskService = polypharmacyRiskService;
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Check polypharmacy risk for a medication list by patient ID.
     *
     * @param patientId ID of the patient
     * @param medicationIds IDs of medications to assess
     * @return polypharmacy risk assessment record
     */
    @PostMapping("/check-risk")
    public ResponseEntity<PolypharmacyRisk> checkPolypharmacyRisk(
            @RequestParam("patientId") Long patientId,
            @RequestParam("medicationIds") List<Long> medicationIds) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Medication> medications = medicationIds.stream()
                .map(id -> medicationRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Medication not found with ID: " + id)))
                .collect(Collectors.toList());

        PolypharmacyRisk risk = polypharmacyRiskService.checkPolypharmacyRisk(medications, null);
        return ResponseEntity.ok(risk);
    }

    /**
     * Get polypharmacy risk record by ID.
     *
     * @param id risk record ID
     * @return risk record
     */
    @GetMapping("/{id}")
    public ResponseEntity<PolypharmacyRisk> getRiskById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(m -> ResponseEntity.ok(new PolypharmacyRisk()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent polypharmacy risk records.
     *
     * @return list of recent risk records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PolypharmacyRisk>> listRecentRisks() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}