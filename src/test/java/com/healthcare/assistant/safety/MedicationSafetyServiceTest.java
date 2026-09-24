package com.healthcare.assistant.safety;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MedicationSafetyServiceTest {

    private final MedicationSafetyService safetyService = new MedicationSafetyService();

    @Test
    @DisplayName("Prohibited medication should be identified as not allowed")
    void prohibitedMedicationShouldBeRejected() {
        assertFalse(safetyService.isMedicationAllowed("isosorbide"));
        assertFalse(safetyService.isMedicationAllowed("Sildenafil"));
    }

    @Test
    @DisplayName("Allowed medication should be accepted")
    void allowedMedicationShouldBeAccepted() {
        assertTrue(safetyService.isMedicationAllowed("acetaminophen"));
        assertTrue(safetyService.isMedicationAllowed("lisinopril"));
    }

    @Test
    @DisplayName("Safety note appended when medication is prohibited")
    void safetyNoteShouldContainWarning() {
        String note = safetyService.getSafetyNote("isosorbide");
        assertTrue(note.contains("NOTIFIED"));
    }
}