package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SchedulingProviderOptimizationController.class)
@WithMockUser
class SchedulingProviderOptimizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getProviderOptimization_returnsOkAndMessage() throws Exception {
        mockMvc.perform(get("/api/v1/scheduling/provider/1/optimization")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Optimization for provider 1"));
    }
}