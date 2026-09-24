package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Consent;
import com.healthcare.assistant.service.ConsentService;
import com.healthcare.assistant.service.PatientContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/consents")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private PatientContextService patientContextService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientConsents(@PathVariable Long patientId) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!patientId.equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Consent> consents = consentService.getPatientConsents(patientId);
        if (consents.isPresent()) {
            return ResponseEntity.ok(consents.get());
        }
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @PostMapping
    public ResponseEntity<Consent> createConsent(@RequestBody Consent consent) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        Consent created = consentService.createConsent(currentPatientId, consent.getType(), consent.getActive());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revokeConsent(@RequestBody Consent consent) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        Optional<Consent> existing = consentService.getPatientConsents(currentPatientId);
        if (existing.isPresent() && !existing.get().getId().equals(consent.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        consentService.revokeConsent(consent.getId());
        return ResponseEntity.noContent().build();
    }
}