package com.healthcare.assistant.controller;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import com.healthcare.assistant.service.ReminderService;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(ReminderController.class)
        @WithMockUser
        class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;

    @Test
    public void scheduleReminder_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/reminders/schedule")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"reminderType\":\"Medication\",\"scheduledDateTime\":\"2025-01-01T10:00:00\"}"))
                .andExpect(status().isNoContent());
    }
}