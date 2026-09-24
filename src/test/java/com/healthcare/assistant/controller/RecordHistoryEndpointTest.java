package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Record;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.repository.RecordRepository;
import com.healthcare.assistant.service.PatientContextService;

/**
 * Phase 3d endpoint coverage: RecordHistory GET + POST through the HTTP layer.
 */
@WebMvcTest(RecordHistoryController.class)
@WithMockUser(roles = "USER")
class RecordHistoryEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordRepository recordRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private PatientContextService patientContextService;

    @Test
    void getHistory_returnsFilteredRecords() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        Record record = new Record();
        record.setId(100L);
        record.setPatient(patient);
        record.setType("Follow-up");
        when(recordRepository.findAll()).thenReturn(Collections.singletonList(record));
        mockMvc.perform(get("/api/v1/records/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("Follow-up"));
    }

    @Test
    void getHistory_noRecords_returnsEmptyArray() throws Exception {
        when(recordRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/records/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createMedicalHistory_returnsCreated() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        Record saved = new Record();
        saved.setId(100L);
        saved.setPatient(patient);
        saved.setType("Follow-up");
        saved.setContent("Feeling better.");
        when(recordRepository.save(any(Record.class))).thenReturn(saved);
        mockMvc.perform(post("/api/v1/records/history/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"Follow-up\",\"content\":\"Feeling better.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("Follow-up"));
    }

    @Test
    void createMedicalHistory_invalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/records/history/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMedicalHistory_unknownPatient_returnsNotFound() throws Exception {
        when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/v1/records/history/999").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"Follow-up\",\"content\":\"Feeling better.\"}"))
                .andExpect(status().isNotFound());
    }
}
