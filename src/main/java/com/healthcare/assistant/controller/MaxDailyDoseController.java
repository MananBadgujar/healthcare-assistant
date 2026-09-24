package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.MaxDailyDose;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.MaxDailyDoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cds/max-daily-dose")
@PreAuthorize("hasRole('PROVIDER')")
public class MaxDailyDoseController {

    private final MaxDailyDoseService maxDailyDoseService;
    private final MedicationRepository medicationRepository;

    @Autowired
    public MaxDailyDoseController(MaxDailyDoseService maxDailyDoseService,
                                   MedicationRepository medicationRepository) {
        this.maxDailyDoseService = maxDailyDoseService;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Check max daily dose for a medication by ID.
     *
     * @param medicationId ID of the medication
     * @param currentDailyDose the current daily dose being taken
     * @return list of max daily dose violations
     */
    @PostMapping("/check-max-dose")
    public ResponseEntity<List<MaxDailyDose>> checkMaxDailyDose(
            @RequestParam("medicationId") Long medicationId,
            @RequestParam("currentDailyDose") double currentDailyDose) {
        Optional<Medication> medicationOpt = medicationRepository.findById(medicationId);
        if (medicationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<MaxDailyDose> exceedances = new ArrayList<>();
        List<MaxDailyDose> violations = maxDailyDoseService.checkMaxDailyDose(
                medicationOpt.get(), currentDailyDose, exceedances);

        return ResponseEntity.ok(violations);
    }

    /**
     * Get max daily dose record by ID.
     *
     * @param id max daily dose record ID
     * @return the max daily dose record details
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaxDailyDose> getMaxDailyDoseById(@PathVariable Long id) {
        Optional<MaxDailyDose> record = Optional.of(new MaxDailyDose());
        return record.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * List recent max daily dose records for audit.
     *
     * @return list of recent max daily dose records
     */
    @GetMapping("/recent")
    public ResponseEntity<List<MaxDailyDose>> listRecentMaxDailyDose() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}