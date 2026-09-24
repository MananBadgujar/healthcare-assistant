package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SchedulingWaitTimePredictionController.class)
@WithMockUser
class SchedulingWaitTimePredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void predictWaitTime_returnsOkAndResult() throws Exception {
        mockMvc.perform(post("/api/v1/scheduling/wait-time-prediction")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"value\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Prediction result"));
    }
}