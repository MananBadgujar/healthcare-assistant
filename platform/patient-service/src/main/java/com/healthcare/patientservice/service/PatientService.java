package com.healthcare.patientservice.service;

import com.healthcare.patientservice.common.AuditService;
import com.healthcare.patientservice.common.EventPublisher;
import com.healthcare.patientservice.entity.Patient;
import com.healthcare.patientservice.repo.PatientRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PatientService {
    private final PatientRepository repo;
    private final AuditService audit;
    private final EventPublisher events;

    public PatientService(PatientRepository repo, AuditService audit, EventPublisher events) {
        this.repo = repo; this.audit = audit; this.events = events;
    }

    @Transactional
    public Patient create(Patient p, String owner) {
        if (p.getFirstName() == null || p.getFirstName().isBlank()) throw new IllegalArgumentException("firstName required");
        if (p.getLastName() == null || p.getLastName().isBlank()) throw new IllegalArgumentException("lastName required");
        p.setId(null);
        p.setOwnerUsername(owner);
        Patient saved = repo.save(p);
        audit.record(owner, "CREATE", "Patient", String.valueOf(saved.getId()), "SUCCESS");
        try {
            events.publish(Topics.PATIENT_CREATED, DomainEvent.of("patient.created", "Patient",
                    String.valueOf(saved.getId()), "patient-service", MDC.get("correlationId"),
                    Map.of("owner", owner == null ? "" : owner)));
        } catch (Exception ignored) {}
        return saved;
    }

    public Patient get(Long id, String requester, boolean privileged) {
        Patient p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Patient not found"));
        if (!privileged && (p.getOwnerUsername() == null || !p.getOwnerUsername().equals(requester)))
            throw new org.springframework.security.access.AccessDeniedException("Not your patient record");
        return p;
    }

    @Transactional
    public Patient update(Long id, Patient patch) {
        Patient p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Patient not found"));
        if (patch.getFirstName() != null) p.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) p.setLastName(patch.getLastName());
        if (patch.getDateOfBirth() != null) p.setDateOfBirth(patch.getDateOfBirth());
        if (patch.getGender() != null) p.setGender(patch.getGender());
        Patient saved = repo.save(p);
        try {
            events.publish(Topics.PATIENT_UPDATED, DomainEvent.of("patient.updated", "Patient",
                    String.valueOf(saved.getId()), "patient-service", MDC.get("correlationId"), Map.of()));
        } catch (Exception ignored) {}
        return saved;
    }
}
