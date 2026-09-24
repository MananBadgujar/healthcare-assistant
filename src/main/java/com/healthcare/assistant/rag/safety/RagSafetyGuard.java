package com.healthcare.assistant.rag.safety;

import com.healthcare.assistant.safety.MedicationSafetyService;
import com.healthcare.assistant.service.HealthcareScopeService;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

/**
 * RAG-specific safety guardrails.
 * <p>
 * Built on top of the existing Phase 7/8 safety infrastructure rather than
 * duplicating it. Specifically:
 * <ul>
 *   <li>Health-only domain restriction reuses {@link HealthcareScopeService}.</li>
 *   <li>Medication safety reuses {@link MedicationSafetyService}.</li>
 *   <li>Emergency/red-flag escalation is detected here so an emergency
 *       question is never answered as routine educational content and instead
 *       returns an immediate action message telling the patient to seek
 *       emergency care.</li>
 * </ul>
 * The guard produces a {@link SafetyDecision} the orchestrator uses to short
 * circuit the RAG flow when the question is unsafe or out of domain.
 */
@Service
public class RagSafetyGuard {

    /** Short, consistent emergency escalation message. */
    public static final String EMERGENCY_MESSAGE =
            "Your description may indicate a medical emergency. Please call your local emergency " +
            "number (for example 911) or go to the nearest emergency department immediately. " +
            "Do not wait for an answer from this service.";

    /** Out-of-domain refusal message. */
    public static final String OUT_OF_DOMAIN_MESSAGE =
            "I can only help with general healthcare education questions. Please rephrase your " +
            "question to focus on health, symptoms, medications, or conditions.";

    private static final Set<String> EMERGENCY_KEYWORDS = Set.of(
            "chest pain", "can't breathe", "cannot breathe", "shortness of breath",
            "unconscious", "unresponsive", "severe bleeding", "stroke", "heart attack",
            "suicidal", "suicide", "overdose", "not breathing", "blue lips", "blue face",
            "choking", "severe allergic reaction", "anaphylaxis", "seizure", "convulsion"
    );

    private static final Set<String> EMERGENCY_MODIFIERS = Set.of(
            "severe", "sudden", "heavy", "uncontrollable", "extreme"
    );

    private final HealthcareScopeService healthcareScopeService;
    private final MedicationSafetyService medicationSafetyService;

    public RagSafetyGuard(HealthcareScopeService healthcareScopeService,
                          MedicationSafetyService medicationSafetyService) {
        this.healthcareScopeService = healthcareScopeService;
        this.medicationSafetyService = medicationSafetyService;
    }

    public SafetyDecision evaluate(String query) {
        if (query == null || query.isBlank()) {
            return SafetyDecision.reject("Question cannot be empty.", "EMPTY_QUESTION");
        }

        // Emergency escalation takes priority over everything else.
        if (looksLikeEmergency(query)) {
            return SafetyDecision.escalate(EMERGENCY_MESSAGE, "EMERGENCY");
        }

        // Healthcare-only domain restriction.
        if (!healthcareScopeService.isHealthcareRelated(query)) {
            return SafetyDecision.reject(OUT_OF_DOMAIN_MESSAGE, "OUT_OF_DOMAIN");
        }

        // Medication safety handling for prescriptive language.
        if (looksLikeMedicationInstruction(query)
                && medicationSafetyService.isPrescriptionSuggestion(query)) {
            return SafetyDecision.escalate(
                    "Medication dosage decisions should be made by a qualified clinician. " +
                    "Please contact your healthcare provider before changing how you take any medication.",
                    "MEDICATION_SAFETY");
        }
        return SafetyDecision.proceed();
    }

    private boolean looksLikeEmergency(String query) {
        String lower = query.toLowerCase(Locale.ROOT);
        if (EMERGENCY_KEYWORDS.stream().anyMatch(lower::contains)) {
            return true;
        }
        // Modifier + emergency symptom heuristic.
        boolean hasModifier = EMERGENCY_MODIFIERS.stream().anyMatch(lower::contains);
        boolean hasSymptom = lower.matches(".*(chest pain|bleeding|breath|dizz|faint|pain|headache).*");
        return hasModifier && hasSymptom && lower.matches(".*(sudden|severe|extreme).*");
    }

    private boolean looksLikeMedicationInstruction(String query) {
        if (query == null) {
            return false;
        }
        String lower = query.toLowerCase(Locale.ROOT);
        return lower.matches(".*(how much|how many|dosage|dose|mg|ml|take|increase|decrease|stop).*");
    }

    /**
     * Result of a safety evaluation.
     *
     * @param proceed    whether the RAG flow may continue with retrieval
     * @param message    when not proceeding, the message returned to the user
     * @param reasonCode short machine-friendly code describing the decision
     */
    public static final class SafetyDecision {
        private final boolean proceed;
        private final String message;
        private final String reasonCode;

        private SafetyDecision(boolean proceed, String message, String reasonCode) {
            this.proceed = proceed;
            this.message = message;
            this.reasonCode = reasonCode;
        }

        static SafetyDecision proceed() {
            return new SafetyDecision(true, null, "PROCEED");
        }

        static SafetyDecision reject(String message, String reasonCode) {
            return new SafetyDecision(false, message, reasonCode);
        }

        static SafetyDecision escalate(String message, String reasonCode) {
            return new SafetyDecision(false, message, reasonCode);
        }

        public boolean isProceed() { return proceed; }
        public String getMessage() { return message; }
        public String getReasonCode() { return reasonCode; }
    }
}
