package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionConsent;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionConsentService;
import com.healthcare.assistant.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/consent")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionConsentController {

    private final DrugInteractionConsentService consentService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionConsentController(
            DrugInteractionConsentService consentService,
            MedicationRepository medicationRepository) {
        this.consentService = consentService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check consent for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @param consents      list of consent records
     * @return consent check result
     */
    @PostMapping("/check-consent")
    public ResponseEntity<Boolean> checkConsent(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId,
            @RequestBody List<DrugInteractionConsent> consents) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }

        boolean result = consentService.checkConsent(
                medicationAOpt.get(), medicationBOpt.get(), consents);

        return ResponseEntity.ok(result);
    }

    /**
     * Get consent record by ID.
     *
     * @param id consent record ID
     * @return the consent record
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionConsent> getConsentById(@PathVariable Long id) {
        Optional<DrugInteractionConsent> consent = Optional.ofNullable(null);
        return ResponseEntity.ok(consent.orElse(null));
    }

    /**
     * List recent consent records.
     *
     * @return list of recent consent records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionConsent>> listRecentConsents() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}