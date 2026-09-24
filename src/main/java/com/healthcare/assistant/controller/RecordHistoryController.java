package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.MedicalHistoryRequest;
import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.entity.Record;
import com.healthcare.assistant.repository.RecordRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/records/history")
public class RecordHistoryController {

    private final RecordRepository repository;
    private final PatientRepository patientRepository;
    private final PatientContextService patientContextService;

    @Autowired
    public RecordHistoryController(RecordRepository repository,
                                   PatientRepository patientRepository,
                                   PatientContextService patientContextService) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.patientContextService = patientContextService;
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<Record>> getHistory(@PathVariable Long patientId) {
        List<Record> allRecords = repository.findAll();
        // Filter records to return only those belonging to the requested patient
        List<Record> filteredRecords = allRecords.stream()
                .filter(r -> r.getPatient() != null && r.getPatient().getId().equals(patientId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filteredRecords);
    }

    @PostMapping("/{patientId}")
    public ResponseEntity<Record> createMedicalHistory(@PathVariable Long patientId,
                                                       @RequestBody @Valid MedicalHistoryRequest request) {
        // Validate request payload
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getType().length() > 100) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getContent().length() > 2000) {
            return ResponseEntity.badRequest().build();
        }

        // Authenticate and get current patient context
        PatientContext currentContext = patientContextService.getCurrentPatientContext();

        // Fetch patient entity based on the path parameter
        var patientOptional = patientRepository.findById(patientId);
        if (patientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Patient patient = patientOptional.get();

        // Enforce that the patient belongs to the authenticated user (emergency‑override retained)
        if (!Long.valueOf(currentContext.getId()).equals(patient.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Create and persist record
        Record record = new Record();
        record.setPatient(patient);
        record.setType(request.getType());
        record.setContent(request.getContent());
        record.setCreatedAt(LocalDateTime.now());
        Record saved = repository.save(record);
        return ResponseEntity.status(201).body(saved);
    }
}