package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for handling missing provider endpoints.
 * Verifies that requests to non-existent provider URLs return a 404 status.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProviderControllerMissingEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void missingProviderEndpoint_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/providers/nonexistent"))
               .andExpect(status().isNotFound());
    }
}