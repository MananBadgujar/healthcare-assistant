package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Consent;
import com.healthcare.assistant.entity.Patient;
import static org.mockito.Mockito.when;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.MedicationAdherence;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.service.ConsentService;
import com.healthcare.assistant.service.MedicationAdherenceService;
import com.healthcare.assistant.service.MedicationService;
import com.healthcare.assistant.service.PatientContextService;

/**
 * Phase 3a endpoint coverage: Consent (GET/revoke), AdherenceScore 2nd base,
 * Medication (GET/PATCH/DELETE), MedicationAdherence (all 3).
 */
@WebMvcTest({ConsentController.class, AdherenceScoreController.class, MedicationController.class,
        MedicationAdherenceController.class})
@WithMockUser(roles = {"USER", "ADMIN", "PROVIDER"})
class ConsentMedicationEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private MedicationService medicationService;

    @MockBean
    private PatientContextService patientContextService;

    @BeforeEach
    void setUp() {
        when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
    }

    @MockBean
    private MedicationAdherenceService adherenceService;

    @MockBean
    private MedicationRepository medicationRepository;

    @Test
    void getPatientConsents_returnsOk() throws Exception {
        when(consentService.getPatientConsents(1L))
                .thenReturn(Optional.of(new Consent(1L, "privacy", true)));
        mockMvc.perform(get("/api/v1/consents/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("privacy"));
    }

@Test
    void getPatientConsents_empty_returnsOk() throws Exception {
        when(consentService.getPatientConsents(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/consents/patient/1"))
                .andExpect(status().isOk());
    }

    @Test
    void revokeConsent_returnsNoContent() throws Exception {
        Consent consent = new Consent(1L, "privacy", true);
        consent.setId(5L);
        mockMvc.perform(post("/api/v1/consents/revoke").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":5}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAdherenceScore_secondBasePath_returnsOk() throws Exception {
        when(medicationService.calculateAdherenceScore(1L)).thenReturn(72);
        mockMvc.perform(get("/api/v1/medications/adherence/score/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(72));
    }

    @Test
    void getMedication_returnsOk() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth("1990-01-01");
        patient.setGender("M");
        Medication medication = new Medication("Aspirin", "100 mg", "1 daily", "With food", patient);
        medication.setId(1L);
        when(medicationService.getMedicationById(1L)).thenReturn(Optional.of(medication));
        mockMvc.perform(get("/api/v1/medications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aspirin"));
    }

    @Test
    void getMedication_missing_returnsNotFound() throws Exception {
        when(medicationService.getMedicationById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/medications/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMedication_returnsNoContent() throws Exception {
        Patient patient = new Patient("John", "Doe", "1990-01-01", "M");
        Medication medication = new Medication("Aspirin", "100 mg", "1 daily", "With food", patient);
        when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(medicationService.updateMedication(anyLong(), any(), any(), any(), any(), anyLong()))
                .thenReturn(Optional.of(medication));
        mockMvc.perform(patch("/api/v1/medications/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Aspirin\",\"dosage\":\"100 mg\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMedication_returnsNoContent() throws Exception {
        when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        mockMvc.perform(delete("/api/v1/medications/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void checkAdherence_returnsOk() throws Exception {
        Patient patient = new Patient("John", "Doe", "1990-01-01", "M");
        Medication medication = new Medication("Aspirin", "100 mg", "1 daily", "With food", patient);
        medication.setId(1L);
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        when(adherenceService.checkAdherence(any(), any(), any())).thenReturn(90.0);
        mockMvc.perform(post("/api/v1/medications/adherence/check-adherence").with(csrf())
                .param("medicationId", "1")
                .param("lastTaken", "2025-01-01T10:00:00"))
                .andExpect(status().isOk());
    }

    @Test
    void checkAdherence_unknownMedication_returnsNotFound() throws Exception {
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/v1/medications/adherence/check-adherence").with(csrf())
                .param("medicationId", "999")
                .param("lastTaken", "2025-01-01T10:00:00"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAdherenceById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/medications/adherence/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentAdherence_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/medications/adherence/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void medicationAdherenceRecordShape() throws Exception {
        MedicationAdherence record = new MedicationAdherence();
        record.setAdherencePercentage(88.5);
        when(medicationRepository.findById(3L)).thenReturn(Optional.of(new Medication()));
        when(adherenceService.checkAdherence(any(), any(), any())).thenReturn(88.5);
        mockMvc.perform(post("/api/v1/medications/adherence/check-adherence").with(csrf())
                .param("medicationId", "3")
                .param("lastTaken", "2025-06-01T08:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adherencePercentage").value(88.5));
    }

    @Test
    void consentListShape() throws Exception {
        when(consentService.getPatientConsents(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/consents/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void unusedMedicationServiceStub() throws Exception {
        when(medicationService.getMedicationsByPatientId(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/medications/patient/1"))
                .andExpect(status().isOk());
    }
}
