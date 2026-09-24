package com.healthcare.assistant.triage.controller;

import com.healthcare.assistant.triage.TriageRequest;
import com.healthcare.assistant.service.AiProvider;
import com.healthcare.assistant.service.HealthcareScopeService;
import com.healthcare.assistant.triage.service.SymptomExtractionService;
import com.healthcare.assistant.safety.MedicationSafetyService;
import com.healthcare.assistant.triage.result.SymptomExtractionResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/triage")
public class TriageController {

private final SymptomExtractionService extractionService;
    private final AiProvider aiProvider;
    private final MedicationSafetyService medicationSafetyService;
    private final HealthcareScopeService healthcareScopeService;
    private final ObjectMapper objectMapper;

    public TriageController(SymptomExtractionService extractionService,
                                    AiProvider aiProvider,
                                    MedicationSafetyService medicationSafetyService,
                                    HealthcareScopeService healthcareScopeService,
                                    ObjectMapper objectMapper) {
        this.extractionService = extractionService;
        this.aiProvider = aiProvider;
        this.medicationSafetyService = medicationSafetyService;
        this.healthcareScopeService = healthcareScopeService;
        this.objectMapper = objectMapper;
    }

    /**
     * Accepts a free-form health query and returns structured triage information.
     *
     * @param request user-provided symptom query
     * @return structured result including red‑flag detection and AI‑generated guidance
     */
    @PostMapping
    public ResponseEntity<SymptomExtractionResult> triage(@RequestBody TriageRequest request) {
        // Validate request presence and mandatory fields
        if (request == null || (request.getQuestion() == null || request.getQuestion().trim().isEmpty())) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check if the question is healthcare-related
        if (!healthcareScopeService.isHealthcareRelated(request.getQuestion())) {
            SymptomExtractionResult result = new SymptomExtractionResult();
            result.setClinicalGuidance("I'm a healthcare assistant and can only help with healthcare-related questions. Please ask about symptoms, medical conditions, treatments, or other health concerns.");
            return ResponseEntity.ok(result);
        }

        // 1. Extract structured symptom information and apply safety rules
        SymptomExtractionResult result = extractionService.extractSymptoms(request);

        // 2. If no red flag, optionally obtain AI‑generated clinical guidance with safety override
        if (!result.isEmergency() && !result.isRedFlag()) {
            String aiGuidance = aiProvider.generateResponse(request.getQuestion());

            if (aiGuidance != null) {
                // Use centralized medication safety service for deterministic checks
                boolean unsafeSelfMedication = medicationSafetyService.isSelfMedicationUnsafe(aiGuidance);
                boolean prescriptionSuggestion = medicationSafetyService.isPrescriptionSuggestion(aiGuidance);

                if (unsafeSelfMedication || prescriptionSuggestion) {
                    result.setClinicalGuidance("Safety override: medication recommendation withheld to prevent unsafe self‑prescription.");
                } else {
                    // Check for prohibited medication mentions in the guidance
                    boolean prohibitedFound = false;
                    String lowerGuidance = aiGuidance.toLowerCase();
                    // Check if guidance contains any prohibited medication
                    for (String prohibitedMed : medicationSafetyService.getProhibitedMedications()) {
                        if (lowerGuidance.contains(prohibitedMed.toLowerCase())) {
                            prohibitedFound = true;
                            break;
                        }
                    }

                    if (prohibitedFound) {
                        result.setClinicalGuidance("Safety override: medication recommendation withheld due to prohibited medication reference.");
                    } else {
                        // Validate AI response structure before using it as guidance
                        String validatedGuidance = validateAiResponse(aiGuidance);
                        result.setClinicalGuidance(validatedGuidance);
                    }
                }
            } else {
                result.setClinicalGuidance("");
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Validates AI-generated JSON response against the required schema.
     * Returns a deterministic safe fallback if validation fails.
     *
     * @param json raw AI output
     * @return validated clinicalGuidance or safe fallback message
     */
    private String validateAiResponse(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            // Check required fields presence
            if (!node.has("intent") || !node.has("symptoms") || !node.has("severity")
                    || !node.has("redFlags") || !node.has("emergency")
                    || !node.has("triageLevel") || !node.has("clinicalGuidance")
                    || !node.has("medicationSafety") || !node.has("followUp")
                    || !node.has("disclaimer")) {
                return "Safety override: AI response missing required fields.";
            }
            // Validate field types
            if (!node.get("emergency").isBoolean()) {
                return "Safety override: AI emergency field must be boolean.";
            }
            if (!node.get("redFlags").isArray()) {
                return "Safety override: AI redFlags must be an array.";
            }
            if (!node.get("severity").isTextual()) {
                return "Safety override: AI severity must be a string.";
            }
            if (!node.get("triageLevel").isTextual()) {
                return "Safety override: AI triageLevel must be a string.";
            }
            if (!node.get("clinicalGuidance").isTextual() || node.get("clinicalGuidance").asText().isBlank()) {
                return "Safety override: AI clinicalGuidance must be a non‑empty string.";
            }
            // If we reach here, validation succeeded; return the clinicalGuidance content
            return node.get("clinicalGuidance").asText();
        } catch (Exception e) {
            return "Safety override: AI response could not be validated. Please consult a qualified healthcare professional.";
        }
    }
}