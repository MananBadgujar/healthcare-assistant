package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.healthcare.assistant.service.MedicationService;

@RestController
@RequestMapping({"/api/v1/adherence", "/api/v1/medications/adherence"})
public class AdherenceScoreController {

    @Autowired
    private MedicationService medicationService;

    @GetMapping("/score/{patientId}")
    public ResponseEntity<Integer> getAdherenceScore(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicationService.calculateAdherenceScore(patientId));
    }
}