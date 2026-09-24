package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.AdherenceLogRequest;
import com.healthcare.assistant.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdherenceLogController {

    private final MedicationService medicationService;

    @Autowired
    public AdherenceLogController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping("/api/v1/medications/adherence/log")
    public ResponseEntity<String> logAdherence(@RequestBody AdherenceLogRequest request) {
        medicationService.logAdherence(request);
        return ResponseEntity.ok("logged");
    }
}