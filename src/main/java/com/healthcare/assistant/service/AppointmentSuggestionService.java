package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Appointment;
import org.springframework.stereotype.Service;

/**
 * Service for generating AI-based appointment suggestions for patients.
 * <p>
 * This service builds a prompt that includes patient context and uses the Ollama model
 * to obtain recommendations for specialty and appointment scheduling. It respects
 * emergency overrides and ensures that no medication advice is provided unless explicitly requested.
 */
public interface AppointmentSuggestionService {
    /**
     * Generates an appointment suggestion for the current patient.
     * @return an {@link Appointment} object containing the recommended specialty and scheduling details.
     */
    Appointment generateSuggestion();
}