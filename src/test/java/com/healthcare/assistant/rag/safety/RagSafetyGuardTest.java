package com.healthcare.assistant.rag.safety;

import com.healthcare.assistant.safety.MedicationSafetyService;
import com.healthcare.assistant.service.HealthcareScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RagSafetyGuard}. Uses lightweight mocks for the
 * {@link HealthcareScopeService} and {@link MedicationSafetyService}
 * dependencies so the guard's branching logic is exercised deterministically.
 */
class RagSafetyGuardTest {

    private HealthcareScopeService scopeService;
    private MedicationSafetyService medicationSafetyService;
    private RagSafetyGuard guard;

    @BeforeEach
    void setUp() {
        scopeService = mock(HealthcareScopeService.class);
        medicationSafetyService = mock(MedicationSafetyService.class);
        guard = new RagSafetyGuard(scopeService, medicationSafetyService);
        when(medicationSafetyService.isPrescriptionSuggestion("how much insulin should I take daily"))
                .thenReturn(true);
        // By default the medication safety service flags any prescription suggestion.
        when(medicationSafetyService.isPrescriptionSuggestion(
                org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
    }

    @Test
    void emptyQuestionIsRejected() {
        RagSafetyGuard.SafetyDecision decision = guard.evaluate("   ");
        assertFalse(decision.isProceed());
        assertEquals("EMPTY_QUESTION", decision.getReasonCode());
    }

    @Test
    void emergencyKeywordsEscalateAndBypassRetrieval() {
        RagSafetyGuard.SafetyDecision decision = guard.evaluate("I'm having chest pain and can't breathe");
        assertFalse(decision.isProceed());
        assertEquals("EMERGENCY", decision.getReasonCode());
        assertTrue(decision.getMessage().contains("emergency"),
                "emergency message should tell the patient to seek emergency care");
    }

    @Test
    void outOfDomainQuestionIsRejected() {
        when(scopeService.isHealthcareRelated("What is the capital of France?")).thenReturn(false);
        RagSafetyGuard.SafetyDecision decision = guard.evaluate("What is the capital of France?");
        assertFalse(decision.isProceed());
        assertEquals("OUT_OF_DOMAIN", decision.getReasonCode());
    }

    @Test
    void medicationInstructionRequestIsEscalated() {
        when(scopeService.isHealthcareRelated("how much insulin should I take daily"))
                .thenReturn(true);
        RagSafetyGuard.SafetyDecision decision = guard.evaluate("how much insulin should I take daily");
        assertFalse(decision.isProceed());
        assertEquals("MEDICATION_SAFETY", decision.getReasonCode());
    }

    @Test
    void safeEducationalQuestionProceeds() {
        when(scopeService.isHealthcareRelated("What is diabetes?")).thenReturn(true);
        RagSafetyGuard.SafetyDecision decision = guard.evaluate("What is diabetes?");
        assertTrue(decision.isProceed());
        assertEquals("PROCEED", decision.getReasonCode());
    }
}
