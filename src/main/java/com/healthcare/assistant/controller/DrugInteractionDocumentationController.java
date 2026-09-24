package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionDocumentation;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.DrugInteractionDocumentationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/drug-interaction-documentation")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionDocumentationController {

    private final DrugInteractionDocumentationService documentationService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionDocumentationController(
            DrugInteractionDocumentationService documentationService,
            MedicationRepository medicationRepository) {
        this.documentationService = documentationService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Document interaction for two medications by ID.
     *
     * @param medicationAId ID of the first medication
     * @param medicationBId ID of the second medication
     * @param patientId ID of the patient
     * @return documentation record
     */
    @PostMapping("/document")
    public ResponseEntity<DrugInteractionDocumentation> documentInteraction(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId,
            @RequestParam("patientId") Long patientId) {

        Optional<Medication> medAOpt = medicationRepository.findByIdAndPatientId(medicationAId, patientId);
        Optional<Medication> medBOpt = medicationRepository.findByIdAndPatientId(medicationBId, patientId);

        if (medAOpt.isEmpty() || medBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new DrugInteractionDocumentation());
        }

        Medication medicationA = medAOpt.get();
        Medication medicationB = medBOpt.get();

        List<DrugInteractionDocumentation> documentations = new ArrayList<>();
        documentationService.documentInteraction(medicationA, medicationB, documentations);

        return ResponseEntity.ok(documentations.get(0));
    }

    /**
     * Get documentation record by ID.
     *
     * @param id documentation record ID
     * @return documentation record or not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionDocumentation> getDocumentationById(@PathVariable Long id) {
        return ResponseEntity.ok(new DrugInteractionDocumentation());
    }

    /**
     * List recent documentation records.
     *
     * @return list of recent documentation records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionDocumentation>> listRecentDocumentations() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}