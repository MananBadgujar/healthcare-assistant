package com.healthcare.assistant.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to check if a question is healthcare-related.
 */
public interface HealthcareScopeService {

    /**
     * Returns true if the question is healthcare-related.
     *
     * @param question the user question
     * @return true if healthcare-related, false otherwise
     */
    boolean isHealthcareRelated(String question);
}