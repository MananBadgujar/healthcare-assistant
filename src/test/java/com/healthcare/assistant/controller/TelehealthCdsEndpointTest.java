package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.TelehealthSession;
import com.healthcare.assistant.service.TelehealthService;

/**
 * Phase 4a endpoint coverage: Telehealth (6) + lightweight CDS (5).
 */
@WebMvcTest({TelehealthController.class, CdsContraindicationsCheckController.class,
        CdsDrugInteractionsController.class, CdsOrderSetController.class,
        CdsRecommendationsController.class, CdsTreatmentRecommendationsController.class})
@WithMockUser(roles = "USER")
class TelehealthCdsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelehealthService telehealthService;

    private TelehealthSession session() {
        TelehealthSession session = new TelehealthSession();
        session.setId(1L);
        session.setStatus("SCHEDULED");
        session.setScheduledStart(LocalDateTime.of(2025, 1, 1, 10, 0));
        session.setScheduledEnd(LocalDateTime.of(2025, 1, 1, 10, 30));
        return session;
    }

    @Test
    void createSession_returnsCreated() throws Exception {
        when(telehealthService.createSession(any(TelehealthSession.class))).thenReturn(session());
        mockMvc.perform(post("/api/v1/telehealth/sessions").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduledStart\":\"2025-01-01T10:00:00\",\"scheduledEnd\":\"2025-01-01T10:30:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void getSession_returnsOk() throws Exception {
        when(telehealthService.getSessionById(1L)).thenReturn(session());
        mockMvc.perform(get("/api/v1/telehealth/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getSession_missing_returnsNotFound() throws Exception {
        when(telehealthService.getSessionById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/v1/telehealth/sessions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSessionStatus_returnsOk() throws Exception {
        TelehealthSession updated = session();
        updated.setStatus("COMPLETED");
        when(telehealthService.updateSessionStatus(anyLong(), anyString())).thenReturn(updated);
        mockMvc.perform(patch("/api/v1/telehealth/sessions/1/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"COMPLETED\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getSessionsByPatient_returnsOk() throws Exception {
        when(telehealthService.getSessionsByPatientId(1L))
                .thenReturn(Collections.singletonList(session()));
        mockMvc.perform(get("/api/v1/telehealth/sessions/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSessionsByProvider_returnsOk() throws Exception {
        when(telehealthService.getSessionsByProviderId(1L))
                .thenReturn(Collections.singletonList(session()));
        mockMvc.perform(get("/api/v1/telehealth/sessions/provider/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSessionsByStatus_returnsOk() throws Exception {
        when(telehealthService.getSessionsByStatus("SCHEDULED"))
                .thenReturn(Collections.singletonList(session()));
        mockMvc.perform(get("/api/v1/telehealth/sessions/status/SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void cdsContraindicationsCheck_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/cds/contraindications-check").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void cdsDrugInteractionsAnalyze_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/cds/drug-interactions/analyze").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void cdsOrderSet_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/cds/order-set").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void cdsRecommendation_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/cds/recommendations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void cdsTreatmentRecommendation_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/cds/treatment-recommendations").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }
}
