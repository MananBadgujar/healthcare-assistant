package com.healthcare.assistant.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.service.RecordService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.dto.SummaryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecordController.class)
@WithMockUser
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

@MockBean
    private RecordService recordService;

    @MockBean
    private PatientContextService patientContextService;

    @BeforeEach
    void setup() {
        PatientContext patientContext = new PatientContext();
        patientContext.setId(1L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
    }

    @Test
    public void createSummary_returnsOk() throws Exception {
        SummaryRequest request = new SummaryRequest(1L, "Patient is stable");
        // Stub RecordService.createSummary to avoid missing bean
        when(recordService.createSummary(any(), any())).thenReturn(new Record());
        mockMvc.perform(post("/api/v1/records/summary")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
