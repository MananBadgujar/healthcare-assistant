package com.healthcare.assistant.rag.dto;

import com.healthcare.assistant.entity.EducationConversation;
import com.healthcare.assistant.entity.EducationFeedback;
import com.healthcare.assistant.entity.EducationMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serialisable view of an education conversation and its messages for the
 * {@code GET /api/v1/kb/conversations/{conversationId}/messages} and the
 * history endpoints. Exposes only the non-sensitive fields; the citations are
 * returned as a parsed JSON string so the client can render them.
 */
public class ConversationDto {

    private final String conversationId;
    private final String principalName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<MessageDto> messages;

    public ConversationDto(EducationConversation conversation, List<EducationMessage> messages) {
        this.conversationId = conversation.getExternalConversationId();
        this.principalName = mask(conversation.getPrincipalName());
        this.createdAt = conversation.getCreatedAt();
        this.updatedAt = conversation.getUpdatedAt();
        this.messages = messages.stream().map(MessageDto::new).collect(Collectors.toList());
    }

    public String getConversationId() { return conversationId; }
    public String getPrincipalName() { return principalName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<MessageDto> getMessages() { return messages; }

    private static String mask(String principal) {
        if (principal == null || principal.isBlank()) {
            return principal;
        }
        int at = principal.indexOf('@');
        if (at > 0) {
            return principal.substring(0, Math.min(2, at)) + "***" + principal.substring(at);
        }
        return principal.substring(0, Math.min(2, principal.length())) + "***";
    }

    public static class MessageDto {
        private final Long id;
        private final String role;
        private final String messageText;
        private final String citationsJson;
        private final boolean grounded;
        private final boolean requiresProviderReview;
        private final String confidence;
        private final LocalDateTime createdAt;
        private final List<FeedbackDto> feedback;

        public MessageDto(EducationMessage message) {
            this.id = message.getId();
            this.role = message.getRole();
            this.messageText = message.getMessageText();
            this.citationsJson = message.getCitationsJson();
            this.grounded = message.isGrounded();
            this.requiresProviderReview = message.isRequiresProviderReview();
            this.confidence = message.getConfidence();
            this.createdAt = message.getCreatedAt();
            this.feedback = message.getFeedback() == null ? java.util.Collections.emptyList()
                    : message.getFeedback().stream().map(FeedbackDto::new).collect(Collectors.toList());
        }

        public Long getId() { return id; }
        public String getRole() { return role; }
        public String getMessageText() { return messageText; }
        public String getCitationsJson() { return citationsJson; }
        public boolean isGrounded() { return grounded; }
        public boolean isRequiresProviderReview() { return requiresProviderReview; }
        public String getConfidence() { return confidence; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public List<FeedbackDto> getFeedback() { return feedback; }
    }

    public static class FeedbackDto {
        private final Long id;
        private final String category;
        private final String comment;
        private final String principalName;
        private final LocalDateTime createdAt;

        public FeedbackDto(EducationFeedback fb) {
            this.id = fb.getId();
            this.category = fb.getCategory();
            this.comment = fb.getComment();
            this.principalName = mask(fb.getPrincipalName());
            this.createdAt = fb.getCreatedAt();
        }

        public Long getId() { return id; }
        public String getCategory() { return category; }
        public String getComment() { return comment; }
        public String getPrincipalName() { return principalName; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
