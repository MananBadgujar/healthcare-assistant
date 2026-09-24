package com.healthcare.assistant.controller;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.healthcare.assistant.service.MedicationService;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdherenceScoreController.class)
@WithMockUser
@Import(com.healthcare.assistant.service.impl.MedicationServiceImpl.class)
public class AdherenceScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    @Test
    public void getAdherenceScore_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/adherence/score/1").with(csrf()))
                .andExpect(status().isOk()).andExpect(content().string("0"));
    }
}