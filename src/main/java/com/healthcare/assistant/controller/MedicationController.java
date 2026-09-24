package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.service.MedicationService;
import com.healthcare.assistant.service.PatientContextService;
import jakarta.validation.Valid;
import com.healthcare.assistant.entity.Medication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.healthcare.assistant.dto.AdherenceLogRequest;
import com.healthcare.assistant.dto.RefillRequest;
import java.util.List;
import java.util.Optional;
import com.healthcare.assistant.dto.MedicationRequest;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final MedicationService medicationService;
    private final PatientContextService patientContextService;

    @Autowired
    public MedicationController(MedicationService medicationService, PatientContextService patientContextService) {
        this.medicationService = medicationService;
        this.patientContextService = patientContextService;
    }

    @PostMapping
    public ResponseEntity<String> createMedication(@Valid @RequestBody MedicationRequest request) {
        String id = medicationService.createMedication(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medication> getMedication(@PathVariable Long id) {
        Optional<Medication> medication = medicationService.getMedicationById(id);
        if (medication.isPresent()) {
            Long medicationPatientId = medication.get().getPatient().getId();
            Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
            if (!medicationPatientId.equals(currentPatientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return medication.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Medication>> getMedicationsByPatientId(@PathVariable Long patientId) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!patientId.equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Medication> medications = medicationService.getMedicationsByPatientId(patientId);
        return ResponseEntity.ok(medications);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateMedication(@PathVariable Long id, @Valid @RequestBody UpdateMedicationRequest request) {
        PatientContext currentContext = patientContextService.getCurrentPatientContext();
        Long patientId = currentContext.getId();
        Optional<Medication> updated = medicationService.updateMedication(
                id,
                request.getName(),
                request.getDosage(),
                request.getFrequency(),
                request.getInstructions(),
                patientId
        );
        return updated.isPresent() ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        PatientContext currentContext = patientContextService.getCurrentPatientContext();
        Long patientId = currentContext.getId();
        medicationService.deleteMedication(id, patientId);
        return ResponseEntity.noContent().build();
    }

    // DTOs for request bodies
    public static class CreateMedicationRequest {
        private String name;
        private String dosage;
        private String frequency;
        private String instructions;
        private Long patientId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }

        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
    }

    public static class UpdateMedicationRequest {
        private String name;
        private String dosage;
        private String frequency;
        private String instructions;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
    }
}