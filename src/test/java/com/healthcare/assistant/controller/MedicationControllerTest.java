package com.healthcare.assistant.controller;

import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.healthcare.assistant.dto.AdherenceLogRequest;
import com.healthcare.assistant.dto.MedicationRequest;
import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.dto.RefillRequest;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.service.MedicationService;
import com.healthcare.assistant.service.PatientContextService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest({MedicationController.class, AdherenceScoreController.class, AdherenceLogController.class, RefillRequestController.class})
        @Import(com.healthcare.assistant.service.impl.MedicationServiceImpl.class)
        @WithMockUser(roles = "ADMIN")
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    @MockBean
    private PatientContextService patientContextService;

    @Test
    public void createMedication_returnsId() throws Exception {
        when(medicationService.createMedication(any(MedicationRequest.class))).thenReturn("med-123");
        // Mock the patient context service to return a patient context with id 1
        PatientContext patientContext = new PatientContext();
        patientContext.setId(1L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
        mockMvc.perform(post("/api/v1/medications")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"medicationName\":\"Paracetamol\",\"dosage\":\"500mg\",\"frequency\":\"1 times\",\"instructions\":\"Take with water\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("med-123"));
    }

    @Test
    public void getMedicationsByPatient_returnsOk() throws Exception {
        PatientContext patientContext = new PatientContext();
        patientContext.setId(1L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
        when(medicationService.getMedicationsByPatientId(1L)).thenReturn(new ArrayList<Medication>());
        mockMvc.perform(get("/api/v1/medications/patient/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void logAdherence_returnsOk() throws Exception {
        when(medicationService.logAdherence(any(AdherenceLogRequest.class))).thenReturn("logged");
        mockMvc.perform(post("/api/v1/medications/adherence/log")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"medicationId\":1,\"action\":\"taken\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("logged"));
    }

    @Test
    public void adherenceScore_returnsOk() throws Exception {
        when(medicationService.calculateAdherenceScore(1L)).thenReturn(85);
        mockMvc.perform(get("/api/v1/medications/adherence/score/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void requestRefill_returnsOk() throws Exception {
        when(medicationService.requestRefill(any(RefillRequest.class))).thenReturn("refill-pending");
        mockMvc.perform(post("/api/v1/medications/refill-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"medicationId\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("refill-pending"));
    }
}