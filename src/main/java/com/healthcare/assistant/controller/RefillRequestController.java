package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.RefillRequest;
import com.healthcare.assistant.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefillRequestController {

    private final MedicationService medicationService;

    @Autowired
    public RefillRequestController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping("/api/v1/medications/refill-request")
    public ResponseEntity<String> requestRefill(@RequestBody RefillRequest request) {
        return ResponseEntity.ok(medicationService.requestRefill(request));
    }
}