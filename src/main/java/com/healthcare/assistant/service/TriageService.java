package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.SymptomIntakeRequest;
import com.healthcare.assistant.dto.TriageResponse;

public interface TriageService {
    String intakeSymptom(SymptomIntakeRequest request);
    TriageResponse getTriageResult(String intakeId);
    void submitFeedback(String intakeId, String feedback);
}