package com.healthcare.assistant.controller;

import jakarta.validation.Valid;
import com.healthcare.assistant.service.PatientService;
import com.healthcare.assistant.service.PatientContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import com.healthcare.assistant.entity.Patient;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;
    private final PatientContextService patientContextService;

    @Autowired
    public PatientController(PatientService patientService, PatientContextService patientContextService) {
        this.patientService = patientService;
        this.patientContextService = patientContextService;
    }

    @PostMapping
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient) {
        Optional<Patient> created = patientService.createPatient(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth().toString(),
                patient.getGender()
        );
        return created.map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!id.equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Patient> patient = patientService.getPatientById(id);
        return patient.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{patientId}/update")
    public ResponseEntity<?> updatePatient(
            @PathVariable Long patientId,
            @Valid @RequestBody Patient updatedData) {
        if (!patientId.equals(patientContextService.getCurrentPatientContext().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Patient> updated = patientService.updatePatient(
                patientId,
                updatedData.getFirstName(),
                updatedData.getLastName(),
                updatedData.getDateOfBirth().toString(),
                updatedData.getGender()
        );
        return updated.isPresent() ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}