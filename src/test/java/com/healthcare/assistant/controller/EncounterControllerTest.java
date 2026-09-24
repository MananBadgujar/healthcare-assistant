package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.healthcare.assistant.entity.Encounter;
import com.healthcare.assistant.service.EncounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

@WebMvcTest(EncounterController.class)
@WithMockUser(roles = "USER")
class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EncounterService encounterService;

    @Test
    public void createEncounter_returnsSavedEncounter() throws Exception {
        when(encounterService.saveEncounter(any(Encounter.class))).thenReturn(new Encounter());
        mockMvc.perform(post("/api/v1/encounters")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"providerId\":1,\"appointmentDateTime\":\"2025-01-01T10:00:00\",\"reason\":\"Check-up\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void getEncounterById_returnsOk() throws Exception {
        when(encounterService.getEncounterById(1L)).thenReturn(Optional.of(new Encounter()));
        mockMvc.perform(get("/api/v1/encounters/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllEncounters_returnsOk() throws Exception {
        when(encounterService.getAllEncounters()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/encounters"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateEncounter_returnsOk() throws Exception {
        when(encounterService.saveEncounter(any(Encounter.class))).thenReturn(new Encounter());
        mockMvc.perform(put("/api/v1/encounters/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"patientId\":1,\"providerId\":1,\"appointmentDateTime\":\"2025-01-02T10:00:00\",\"reason\":\"Follow-up\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteEncounter_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/encounters/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}