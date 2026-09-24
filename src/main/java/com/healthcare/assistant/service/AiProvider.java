package com.healthcare.assistant.service;

/**
 * Contract for AI provider operations.
 */
public interface AiProvider {
    /**
     * Generate a response from the AI model.
     *
     * @param prompt the prompt to send to the model
     * @return raw response as string
     */
    String generateResponse(String prompt);
}