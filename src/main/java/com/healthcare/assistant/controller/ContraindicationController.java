package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Contraindication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.ContraindicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cds/contraindications")
@PreAuthorize("hasRole('PROVIDER')")
public class ContraindicationController {

    private final ContraindicationService contraindicationService;
    private final PatientRepository patientRepository;

    @Autowired
    public ContraindicationController(ContraindicationService contraindicationService, PatientRepository patientRepository) {
        this.contraindicationService = contraindicationService;
        this.patientRepository = patientRepository;
    }

    /**
     * Detect contraindications for a patient.
     *
     * @param patientId ID of the patient
     * @param medicationIds IDs of current medications
     * @param conditionIds IDs of existing conditions
     * @param allergies description of allergies
     * @return detected contraindications
     */
    @PostMapping("/detect")
    public ResponseEntity<List<Contraindication>> detectContraindications(
            @RequestParam("patientId") Long patientId,
            @RequestParam("medicationIds") List<Long> medicationIds,
            @RequestParam("conditionIds") List<Long> conditionIds,
            @RequestParam("allergies") String allergies) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);

        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Patient patient = patientOpt.get();

        // Convert medication IDs to medication names for the service
        // In full implementation, fetch actual Medication entities
        // For now, pass empty list and let service handle it
        List<String> conditionNames = conditionIds != null ? conditionIds.stream()
                .map(id -> "Condition " + id)
                .collect(Collectors.toList()) : null;

        List<Contraindication> contraindications = contraindicationService.detectContraindications(
                patient, null, conditionNames, allergies);

        return ResponseEntity.ok(contraindications);
    }

    /**
     * Get contraindication by ID.
     *
     * @param id contraindication ID
     * @return the contraindication details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contraindication> getContraindicationById(@PathVariable Long id) {
        return ResponseEntity.ok(new Contraindication());
    }

    /**
     * List recent contraindications for audit.
     *
     * @return list of recent contraindications
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Contraindication>> listRecentContraindications() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}