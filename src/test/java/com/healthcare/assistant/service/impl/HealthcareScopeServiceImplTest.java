package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.service.HealthcareScopeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HealthcareScopeServiceImplTest {

    @Autowired
    private HealthcareScopeService healthcareScopeService;

    @Test
    @DisplayName("Healthcare-related questions should return true")
    void healthcareQuestionsShouldReturnTrue() {
        assertTrue(healthcareScopeService.isHealthcareRelated("I have a headache and fever"));
        assertTrue(healthcareScopeService.isHealthcareRelated("What are the symptoms of diabetes?"));
        assertTrue(healthcareScopeService.isHealthcareRelated("I need to take my medication"));
        assertTrue(healthcareScopeService.isHealthcareRelated("Can you help me with my back pain?"));
        assertTrue(healthcareScopeService.isHealthcareRelated("I'm feeling dizzy and nauseous"));
        assertTrue(healthcareScopeService.isHealthcareRelated("What is the recommended dosage for aspirin?"));
        assertTrue(healthcareScopeService.isHealthcareRelated("I have chest pain and shortness of breath"));
    }

    @Test
    @DisplayName("Non-healthcare-related questions should return false")
    void nonHealthcareQuestionsShouldReturnFalse() {
        assertFalse(healthcareScopeService.isHealthcareRelated("What are the best vacation spots in Europe?"));
        assertFalse(healthcareScopeService.isHealthcareRelated("How do I fix my computer?"));
        assertFalse(healthcareScopeService.isHealthcareRelated("blorf glorf zlorf"));
        assertFalse(healthcareScopeService.isHealthcareRelated("What's the stock market doing today?"));
        assertFalse(healthcareScopeService.isHealthcareRelated("Tell me a joke"));
        assertFalse(healthcareScopeService.isHealthcareRelated("What's the capital of France?"));
        assertFalse(healthcareScopeService.isHealthcareRelated("How to fix a leaky faucet?"));
    }

    @Test
    @DisplayName("Edge cases: empty and null questions")
    void edgeCases() {
        assertFalse(healthcareScopeService.isHealthcareRelated(""));
        assertFalse(healthcareScopeService.isHealthcareRelated(null));
        assertFalse(healthcareScopeService.isHealthcareRelated("   ")); // only spaces
    }

    @Test
    @DisplayName("Mixed content: healthcare question with non-healthcare context")
    void mixedContent() {
        assertTrue(healthcareScopeService.isHealthcareRelated("I have a headache, but also what's the weather like?"));
        assertTrue(healthcareScopeService.isHealthcareRelated("My arm hurts from playing tennis, speaking of tennis, did you see the match?"));
    }
}