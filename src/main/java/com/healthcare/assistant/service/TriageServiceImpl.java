package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.SymptomIntakeRequest;
import com.healthcare.assistant.dto.TriageResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TriageServiceImpl implements TriageService {

    private final Map<String, TriageResponse> storage = new ConcurrentHashMap<>();
    private final Map<String, SymptomIntakeRequest> requestMap = new ConcurrentHashMap<>();

    @Override
    public String intakeSymptom(SymptomIntakeRequest request) {
        String id = UUID.randomUUID().toString();
        requestMap.put(id, request);
        // Create a default response for demonstration
        TriageResponse response = new TriageResponse();
        response.setSeverity("MILD");
        response.setUrgency("LOW");
        response.setPossibleCategories(List.of("GENERAL"));
        response.setRedFlags(List.of());
        response.setRecommendedNextStep("Observe");
        response.setExplanation("No immediate concerns detected.");
        response.setConfidence(0.8);
        response.setRequiresProfessionalEvaluation(false);
        storage.put(id, response);
        return id;
    }

    @Override
    public TriageResponse getTriageResult(String intakeId) {
        return storage.get(intakeId);
    }

    @Override
    public void submitFeedback(String intakeId, String feedback) {
        // In a real system, store feedback and trigger model retraining
    }
}