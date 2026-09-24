package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.healthcare.assistant.rag.PatientEducationFacade;
import com.healthcare.assistant.rag.dto.AskRequest;
import com.healthcare.assistant.rag.dto.RagResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Slice test for {@link KnowledgeBaseAskController}. The controller's RAG
 * collaborators are mocked so the slice stays fast and deterministic; the test
 * asserts the structured response contract and HTTP status.
 */
@WebMvcTest(KnowledgeBaseAskController.class)
@WithMockUser
class KnowledgeBaseAskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientEducationFacade educationFacade;

    @Test
    void askQuestion_returnsOk() throws Exception {
        RagResponse response = new RagResponse();
        response.setAnswer("Some educational answer.");
        response.setGrounded(true);
        response.setConfidence("MODERATE");
        response.setSources(java.util.Collections.emptyList());
        response.setDisclaimer("This information is for general educational purposes only.");
        when(educationFacade.ask(any(AskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/kb/ask")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"What is diabetes?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Some educational answer."))
                .andExpect(jsonPath("$.grounded").value(true))
                .andExpect(jsonPath("$.confidence").value("MODERATE"));
    }
}
