package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.EducationConversation;
import com.healthcare.assistant.entity.EducationFeedback;
import com.healthcare.assistant.entity.EducationMessage;
import com.healthcare.assistant.rag.conversation.EducationConversationService;
import com.healthcare.assistant.rag.feedback.FeedbackCategory;
import com.healthcare.assistant.rag.feedback.FeedbackService;

/**
 * Phase 9 endpoint coverage: EducationConversation success paths.
 */
@WebMvcTest(EducationConversationController.class)
@WithMockUser(username = "patient@example.com", roles = "USER")
class EducationConversationEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EducationConversationService conversationService;

    @MockBean
    private FeedbackService feedbackService;

    private EducationConversation conversation() {
        EducationConversation conversation = new EducationConversation();
        conversation.setExternalConversationId("conv-1");
        conversation.setPrincipalName("patient@example.com");
        conversation.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        conversation.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        EducationMessage message = new EducationMessage();
        message.setId(1L);
        message.setRole("assistant");
        message.setMessageText("Drink water");
        message.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        conversation.setMessages(Collections.singletonList(message));
        return conversation;
    }

    @Test
    void listConversations_returnsOk() throws Exception {
        when(conversationService.conversationsFor("patient@example.com"))
                .thenReturn(Collections.singletonList(conversation()));
        mockMvc.perform(get("/api/v1/kb/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value("conv-1"));
    }

    @Test
    void listConversations_empty_returnsOk() throws Exception {
        when(conversationService.conversationsFor("patient@example.com"))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/kb/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getConversation_returnsOk() throws Exception {
        EducationConversation conversation = conversation();
        when(conversationService.resolveOrCreate("conv-1", "patient@example.com"))
                .thenReturn(conversation);
        when(conversationService.history(conversation)).thenReturn(conversation.getMessages());
        mockMvc.perform(get("/api/v1/kb/conversations/conv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-1"))
                .andExpect(jsonPath("$.messages[0].messageText").value("Drink water"));
    }

    @Test
    void submitFeedback_returnsCreated() throws Exception {
        EducationFeedback feedback = new EducationFeedback();
        feedback.setId(1L);
        feedback.setCategory(FeedbackCategory.HELPFUL.name());
        feedback.setComment("thanks");
        feedback.setPrincipalName("patient@example.com");
        feedback.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        when(feedbackService.submit(anyLong(), any(), anyString(), anyString())).thenReturn(feedback);
        mockMvc.perform(post("/api/v1/kb/feedback/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"HELPFUL\",\"comment\":\"thanks\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("HELPFUL"));
    }

    @Test
    void submitFeedback_missingCategory_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/kb/feedback/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"thanks\"}"))
                .andExpect(status().isBadRequest());
    }
}
