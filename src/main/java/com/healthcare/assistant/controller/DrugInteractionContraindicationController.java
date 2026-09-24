package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionContraindication;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.DrugInteractionContraindicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/drug-interaction-contraindications")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionContraindicationController {

    private final DrugInteractionContraindicationService contraindicationService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionContraindicationController(
            DrugInteractionContraindicationService contraindicationService,
            MedicationRepository medicationRepository) {
        this.contraindicationService = contraindicationService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check contraindication for two medications by ID.
     *
     * @param medicationAId ID of the first medication
     * @param medicationBId ID of the second medication
     * @param patientId ID of the patient
     * @return detected contraindication record
     */
    @PostMapping("/check-contraindication")
    public ResponseEntity<DrugInteractionContraindication> checkContraindication(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId,
            @RequestParam("patientId") Long patientId) {

        Optional<Medication> medAOpt = medicationRepository.findByIdAndPatientId(medicationAId, patientId);
        Optional<Medication> medBOpt = medicationRepository.findByIdAndPatientId(medicationBId, patientId);

        if (medAOpt.isEmpty() || medBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new DrugInteractionContraindication());
        }

        Medication medicationA = medAOpt.get();
        Medication medicationB = medBOpt.get();

        List<DrugInteractionContraindication> contraindications = new ArrayList<>();
        contraindicationService.checkContraindication(medicationA, medicationB, contraindications);

        if (!contraindications.isEmpty()) {
            return ResponseEntity.ok(contraindications.get(0));
        }

        return ResponseEntity.ok(new DrugInteractionContraindication());
    }

    /**
     * Get contraindication record by ID.
     *
     * @param id contraindication ID
     * @return the contraindication details
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionContraindication> getContraindicationById(@PathVariable Long id) {
        return ResponseEntity.ok(new DrugInteractionContraindication());
    }

    /**
     * List recent contraindication records.
     *
     * @return list of recent contraindication records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionContraindication>> listRecentContraindications() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}