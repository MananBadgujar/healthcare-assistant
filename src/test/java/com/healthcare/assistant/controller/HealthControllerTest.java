package com.healthcare.assistant.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;



import org.springframework.security.test.context.support.WithMockUser;



import org.springframework.beans.factory.annotation.Autowired;

@WithMockUser(roles = "USER") @WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthCheck_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}