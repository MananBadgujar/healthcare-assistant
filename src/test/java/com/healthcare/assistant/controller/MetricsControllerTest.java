package com.healthcare.assistant.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
public class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void metrics_endpoint_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/metrics")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk());
    }
}
