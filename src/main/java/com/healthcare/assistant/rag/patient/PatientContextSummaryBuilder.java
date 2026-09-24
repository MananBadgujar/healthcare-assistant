package com.healthcare.assistant.rag.patient;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.service.PatientContextService;
import org.springframework.stereotype.Component;

/**
 * Builds a short, sanitised summary of the authenticated patient's context for
 * the RAG prompt.
 * <p>
 * Only a minimal, non-sensitive set of attributes (age band derived from date of
 * birth, gender, and a coarse set of relevant conditions when known) is exposed
 * to the LLM. The patient context can never "override" knowledge-base evidence;
 * it is presented only as background (see {@link com.healthcare.assistant.rag.generation.GroundedPromptBuilder}).
 * <p>
 * If no patient isauthenticated, the builder returns an empty string so the
 * pipeline degrades gracefully to general education mode.
 */
@Component
public class PatientContextSummaryBuilder {

    private final PatientContextService patientContextService;

    public PatientContextSummaryBuilder(PatientContextService patientContextService) {
        this.patientContextService = patientContextService;
    }

    /**
     * @return a short patient-context summary, or empty string when the caller
     * is not an authenticated patient
     */
    public String build() {
        PatientContext context;
        try {
            context = patientContextService.getCurrentPatientContext();
        } catch (RuntimeException ex) {
            // Non-patient principals (admin/provider) have no patient context.
            return "";
        }
        if (context == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (context.getDateOfBirth() != null && !context.getDateOfBirth().isBlank()) {
            String ageBand = ageBandFromDob(context.getDateOfBirth());
            if (ageBand != null) {
                sb.append("Patient age band: ").append(ageBand).append(". ");
            }
        }
        if (context.getGender() != null && !context.getGender().isBlank()) {
            sb.append("Patient gender: ").append(context.getGender()).append(".");
        }
        return sb.toString().strip();
    }

    /** Derived age band to avoid sending a precise birth date to the LLM. */
    private String ageBandFromDob(String dateOfBirth) {
        // Accept ISO dates "yyyy-MM-dd" or already-band strings defensively.
        try {
            java.time.LocalDate dob = java.time.LocalDate.parse(dateOfBirth);
            int age = java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
            if (age < 0) {
                return null;
            }
            if (age <= 12) {
                return "child";
            }
            if (age <= 17) {
                return "adolescent";
            }
            if (age <= 39) {
                return "young adult";
            }
            if (age <= 64) {
                return "adult";
            }
            return "senior";
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }
}
