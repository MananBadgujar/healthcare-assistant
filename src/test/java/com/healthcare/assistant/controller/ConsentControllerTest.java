package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.entity.Consent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.service.ConsentService;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.dto.PatientContext;

@WebMvcTest(ConsentController.class)
@WithMockUser
class ConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsentService consentService;

    @MockBean
    private PatientContextService patientContextService;

    @BeforeEach
    void setUp() {
        Mockito.when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "Test", "Patient", "M", "1990-01-01"));
    }

    @TestConfiguration
    static class ConsentServiceTestConfig {
        @Bean
        @Primary
        public ConsentService consentService() {
            return Mockito.mock(ConsentService.class);
        }
    }

@Test
     public void createConsent_returnsOk() throws Exception {
        Consent consent = new Consent(1L, "privacy", true);
        Mockito.when(consentService.createConsent(any(), any(), any()))
               .thenReturn(new Consent(1L, "privacy", true));

        mockMvc.perform(post("/api/v1/consents")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consent)))
                .andExpect(status().isOk());
    }
}