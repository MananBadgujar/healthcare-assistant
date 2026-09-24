package com.healthcare.assistant.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CacheClearController.class)
public class CacheClearControllerTest {

    @Autowired
    private MockMvc mockMvc;

@Test
     public void clearCache_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/cache/clear")
                        .with(csrf())
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isNoContent());
    }
}
