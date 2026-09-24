package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.MedicationAdherence;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.MedicationAdherenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/medications/adherence")
@PreAuthorize("hasRole('PROVIDER')")
public class MedicationAdherenceController {

    private final MedicationAdherenceService adherenceService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public MedicationAdherenceController(MedicationAdherenceService adherenceService,
                                          MedicationRepository medicationRepository) {
        this.adherenceService = adherenceService;
        this.medicationRepository = medicationRepository;
    }

    @PostMapping("/check-adherence")
    public ResponseEntity<MedicationAdherence> checkAdherence(
            @RequestParam Long medicationId,
            @RequestParam("lastTaken") String lastTakenStr) {
        Optional<Medication> medicationOpt = medicationRepository.findById(medicationId);
        if (medicationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Medication medication = medicationOpt.get();
        LocalDateTime lastTaken = LocalDateTime.parse(lastTakenStr);
        MedicationAdherence record = new MedicationAdherence();
        record.setAdherencePercentage(adherenceService.checkAdherence(medication, lastTaken, List.of()));
        record.setMedication(medication);
        record.setLastTaken(lastTaken);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationAdherence> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new MedicationAdherence());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MedicationAdherence>> listRecent() {
        return ResponseEntity.ok(List.of());
    }
}