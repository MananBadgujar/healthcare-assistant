package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.SymptomIntakeRequest;
import com.healthcare.assistant.dto.TriageResponse;
import com.healthcare.assistant.service.TriageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/symptoms")
public class SymptomTriageController {

    @Autowired
    private TriageService triageService;

    @PostMapping("/intake")
    public ResponseEntity<String> intake(@RequestBody SymptomIntakeRequest request) {
        String intakeId = triageService.intakeSymptom(request);
        return ResponseEntity.ok(intakeId);
    }

    @PostMapping("/triage/run")
    public ResponseEntity<TriageResponse> runTriage(@RequestBody Map<String, String> payload) {
        String intakeId = payload.get("intakeId");
        TriageResponse response = triageService.getTriageResult(intakeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/triage/{intakeId}")
    public ResponseEntity<TriageResponse> getTriage(@PathVariable String intakeId) {
        TriageResponse response = triageService.getTriageResult(intakeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/triage/feedback")
    public ResponseEntity<String> submitFeedback(@RequestBody Map<String, String> payload) {
        triageService.submitFeedback(payload.get("intakeId"), payload.get("feedback"));
        return ResponseEntity.ok("Feedback recorded");
    }
}