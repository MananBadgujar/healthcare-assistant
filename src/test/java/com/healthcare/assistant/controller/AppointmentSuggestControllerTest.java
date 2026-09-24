package com.healthcare.assistant.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.service.AppointmentSuggestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

@WebMvcTest(AppointmentSuggestController.class)
@WithMockUser
class AppointmentSuggestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentSuggestionService appointmentSuggestionService;

    @Test
    public void suggestAppointment_returnsOk() throws Exception {
        Appointment request = new Appointment();
        // No need to set patientId or providerId for this test
        request.setAppointmentDateTime(LocalDateTime.parse("2025-01-01T10:00:00"));
        request.setReason("Check-up");

        // Stub the service to return a dummy appointment
        Appointment dummyResponse = new Appointment();
        dummyResponse.setId(1L);
        when(appointmentSuggestionService.generateSuggestion()).thenReturn(dummyResponse);

        mockMvc.perform(post("/api/v1/appointments/suggest")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}