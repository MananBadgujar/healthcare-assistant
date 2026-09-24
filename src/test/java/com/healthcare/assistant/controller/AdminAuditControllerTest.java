package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminAuditController.class)
@WithMockUser(roles = "ADMIN")
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAuditLog_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}