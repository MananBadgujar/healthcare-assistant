package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.healthcare.assistant.service.NotificationService;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(NotificationController.class)
        @WithMockUser
        class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void sendNow_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/sendNow")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":\"123\",\"message\":\"test\"}"))
                .andExpect(status().isOk());
    }
}