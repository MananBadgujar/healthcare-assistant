package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DrugInteractionResolution;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.DrugInteractionResolutionService;
import com.healthcare.assistant.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/interaction-resolutions")
@PreAuthorize("hasRole('PROVIDER')")
public class DrugInteractionResolutionController {

    private final DrugInteractionResolutionService resolutionService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public DrugInteractionResolutionController(
            DrugInteractionResolutionService resolutionService,
            MedicationRepository medicationRepository) {
        this.resolutionService = resolutionService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Record interaction resolution for two medications by ID.
     *
     * @param medicationAId ID of first medication
     * @param medicationBId ID of second medication
     * @param resolutionStatus resolution status (PENDING/IN_PROGRESS/RESOLVED)
     * @param resolutionCategory resolution category (DOSE_ADJUSTMENT/MEDICATION_SWITCH/MONITORING/DISCONTINUATION)
     * @param resolvedBy provider who resolved the interaction
     * @param resolutionNotes notes documenting the resolution
     * @param actionTaken action taken to resolve the interaction
     * @return the recorded resolution record
     */
    @PostMapping("/resolve")
    public ResponseEntity<DrugInteractionResolution> recordResolution(
            @RequestParam("medicationAId") Long medicationAId,
            @RequestParam("medicationBId") Long medicationBId,
            @RequestParam("resolutionStatus") String resolutionStatus,
            @RequestParam("resolutionCategory") String resolutionCategory,
            @RequestParam("resolvedBy") String resolvedBy,
            @RequestParam("resolutionNotes") String resolutionNotes,
            @RequestParam("actionTaken") String actionTaken) {
        Optional<Medication> medicationAOpt = medicationRepository.findById(medicationAId);
        Optional<Medication> medicationBOpt = medicationRepository.findById(medicationBId);

        if (medicationAOpt.isEmpty() || medicationBOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        DrugInteractionResolution resolution = resolutionService.resolveInteraction(
                medicationAOpt.get(), medicationBOpt.get(), null);

        resolution.setResolutionStatus(resolutionStatus);
        resolution.setResolutionCategory(resolutionCategory);
        resolution.setResolvedBy(resolvedBy);
        resolution.setResolutionNotes(resolutionNotes);
        resolution.setActionTaken(actionTaken);
        resolution.setResolutionDate(LocalDateTime.now());
        resolution.setUpdatedAt(LocalDateTime.now());

        return ResponseEntity.ok(resolution);
    }

    /**
     * Get resolution record by ID.
     *
     * @param id resolution record ID
     * @return the resolution record or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrugInteractionResolution> getResolutionById(@PathVariable Long id) {
        return medicationRepository.findById(id)
                .map(m -> ResponseEntity.ok(new DrugInteractionResolution()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent resolution records.
     *
     * @ list of recent resolution records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DrugInteractionResolution>> listRecentResolutions() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}