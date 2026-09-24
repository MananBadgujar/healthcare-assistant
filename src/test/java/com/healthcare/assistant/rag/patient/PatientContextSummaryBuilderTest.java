package com.healthcare.assistant.rag.patient;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.service.PatientContextService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Privacy-focussed unit tests for {@link PatientContextSummaryBuilder}.
 * Verifies that no precise DOB and no raw medical record data leak to the
 * LLM-facing prompt summary.
 */
class PatientContextSummaryBuilderTest {

    @Test
    void emitsAgeBandNotRawDob() {
        PatientContextService service = mock(PatientContextService.class);
        when(service.getCurrentPatientContext()).thenReturn(contextWith(LocalDate.now().minusYears(45).toString(), "F"));
        String summary = new PatientContextSummaryBuilder(service).build();
        assertTrue(summary.contains("Patient age band: adult"),
                "summary must contain a coarse age band only; was: " + summary);
        assertFalse(summary.contains(LocalDate.now().minusYears(45).toString()),
                "summary must NOT contain the raw DOB");
    }

    @Test
    void emitsGenderWhenPresent() {
        PatientContextService service = mock(PatientContextService.class);
        when(service.getCurrentPatientContext()).thenReturn(contextWith(LocalDate.now().minusYears(30).toString(), "Male"));
        String summary = new PatientContextSummaryBuilder(service).build();
        assertTrue(summary.contains("young adult"));
        assertTrue(summary.contains("Male"));
    }

    @Test
    void emptySummaryWhenContextIsNull() {
        PatientContextService service = mock(PatientContextService.class);
        when(service.getCurrentPatientContext()).thenReturn(null);
        String summary = new PatientContextSummaryBuilder(service).build();
        assertEquals("", summary);
    }

    @Test
    void emptySummaryWhenContextThrows() {
        PatientContextService service = mock(PatientContextService.class);
        when(service.getCurrentPatientContext()).thenThrow(new RuntimeException("not a patient"));
        String summary = new PatientContextSummaryBuilder(service).build();
        assertEquals("", summary);
    }

    @Test
    void blankDobIgnored() {
        PatientContextService service = mock(PatientContextService.class);
        when(service.getCurrentPatientContext()).thenReturn(contextWith("   ", "Female"));
        String summary = new PatientContextSummaryBuilder(service).build();
        assertFalse(summary.contains("age band"), "blank DOB must be ignored; was: " + summary);
        assertTrue(summary.contains("Female"));
    }

    @Test
    void ageBandsCoverChildThroughSenior() {
        PatientContextService service = mock(PatientContextService.class);
        String[][] cases = {
                {"10", "child"},
                {"16", "adolescent"},
                {"30", "young adult"},
                {"50", "adult"},
                {"70", "senior"}
        };
        for (String[] c : cases) {
            LocalDate dob = LocalDate.now().minusYears(Integer.parseInt(c[0]));
            when(service.getCurrentPatientContext()).thenReturn(contextWith(dob.toString(), "X"));
            String summary = new PatientContextSummaryBuilder(service).build();
            assertTrue(summary.contains("Patient age band: " + c[1]),
                    "age " + c[0] + " should map to '" + c[1] + "'; was: " + summary);
        }
    }

    @Test
    void negativeAgeIgnored() {
        PatientContextService service = mock(PatientContextService.class);
        // DOB in the future
        when(service.getCurrentPatientContext())
                .thenReturn(contextWith(LocalDate.now().plusYears(5).toString(), "X"));
        String summary = new PatientContextSummaryBuilder(service).build();
        assertFalse(summary.contains("age band"));
    }

    private PatientContext contextWith(String dob, String gender) {
        PatientContext ctx = new PatientContext();
        ctx.setDateOfBirth(dob);
        ctx.setGender(gender);
        return ctx;
    }
}
