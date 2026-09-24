package com.healthcare.medicationservice.web;

import com.healthcare.medicationservice.common.AuditService;
import com.healthcare.medicationservice.common.EventPublisher;
import com.healthcare.medicationservice.entity.Medication;
import com.healthcare.medicationservice.repo.MedicationRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {
    private static final java.util.Set<String> PRESCRIBABLE = java.util.Set.of(
            "paracetamol", "ibuprofen", "amoxicillin", "metformin", "atorvastatin",
            "aspirin", "omeprazole", "salbutamol");
    private final MedicationRepository repo;
    private final EventPublisher events;
    private final AuditService audit;
    public MedicationController(MedicationRepository repo, EventPublisher events, AuditService audit) {
        this.repo = repo; this.events = events; this.audit = audit;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public ResponseEntity<Medication> create(@RequestBody Medication m, Authentication auth) {
        // Safety guardrail: only formulary medications may be prescribed through the API.
        if (m.getName() == null || !PRESCRIBABLE.contains(m.getName().toLowerCase()))
            throw new IllegalArgumentException("Medication not in approved formulary - provider review required");
        if (m.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        m.setId(null); m.setStatus("ACTIVE");
        Medication saved = repo.save(m);
        audit.record(auth.getName(), "PRESCRIBE", "Medication", String.valueOf(saved.getId()), "SUCCESS");
        try {
            events.publish(Topics.MEDICATION_CREATED, DomainEvent.of("medication.created", "Medication",
                    String.valueOf(saved.getId()), "medication-service", MDC.get("correlationId"),
                    Map.of("patientId", String.valueOf(saved.getPatientId()), "name", saved.getName())));
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public List<Medication> byPatient(@PathVariable Long patientId) {
        return repo.findByPatientId(patientId);
    }

    @PostMapping("/{id}/refill")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Medication refill(@PathVariable Long id, Authentication auth) {
        Medication m = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Medication not found"));
        m.setStatus("REFILL_REQUESTED");
        Medication saved = repo.save(m);
        audit.record(auth.getName(), "REFILL", "Medication", String.valueOf(id), "SUCCESS");
        try {
            events.publish(Topics.MEDICATION_REFILL, DomainEvent.of("medication.refill-requested", "Medication",
                    String.valueOf(id), "medication-service", MDC.get("correlationId"), Map.of()));
        } catch (Exception ignored) {}
        return saved;
    }
}
