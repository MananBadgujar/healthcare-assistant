package com.healthcare.assistant.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.healthcare.assistant.entity.KnowledgeBaseArticle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import com.healthcare.assistant.service.KnowledgeBaseArticleService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.dto.KbArticleCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(KnowledgeBaseArticleController.class)
@WithMockUser
class KnowledgeBaseArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KnowledgeBaseArticleService articleService;

@Test
     public void createArticle_returnsOk() throws Exception {
        var request = new KbArticleCreateRequest("Test Title", "Test Content");
        // Stub KnowledgeBaseArticleService.saveArticle to avoid missing bean
        when(articleService.saveArticle(any(KnowledgeBaseArticle.class))).thenReturn(new KnowledgeBaseArticle());
        mockMvc.perform(post("/api/v1/kb/articles")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
