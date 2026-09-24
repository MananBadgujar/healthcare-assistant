package com.healthcare.assistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.rag.dto.SearchHitDto;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.vector.VectorRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

/**
 * Slice test for {@link KnowledgeBaseSearchController}. The semantic search
 * collaborator is mocked so the slice stays focused on the controller's HTTP
 * contract.
 */
@WebMvcTest(KnowledgeBaseSearchController.class)
public class KnowledgeBaseSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SemanticSearchService searchService;

    @Test
    public void search_endpoint_returnsOk() throws Exception {
        when(searchService.search(anyString(), isNull()))
                .thenReturn(Collections.<VectorRecord>emptyList());
        mockMvc.perform(get("/api/v1/kb/search")
                        .param("query", "test")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk());
    }
}
