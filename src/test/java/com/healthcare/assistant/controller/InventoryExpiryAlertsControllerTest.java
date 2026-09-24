package com.healthcare.assistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
@WebMvcTest(InventoryExpiryAlertsController.class)
public class InventoryExpiryAlertsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.junit.jupiter.api.Test
    public void triggerExpiryAlerts_returnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/inventory/expiry-alerts")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNoContent());
    }
}