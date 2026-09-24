package com.healthcare.assistant.rag.conversation;

import com.healthcare.assistant.entity.EducationConversation;
import com.healthcare.assistant.entity.EducationMessage;
import com.healthcare.assistant.rag.config.RagProperties;
import com.healthcare.assistant.repository.EducationConversationRepository;
import com.healthcare.assistant.repository.EducationMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Conversation management for the conversational patient-education flow.
 * <p>
 * Each conversation is persisted with its messages. The history window (number
 * of prior turns retained) is bounded by the configured value, so the LLM never
 * receives unbounded history. Conversations are scoped by the authenticated
 * principal to prevent cross-patient leakage.
 */
@Service
public class EducationConversationService {

    private final EducationConversationRepository conversationRepository;
    private final EducationMessageRepository messageRepository;
    private final RagProperties properties;

    public EducationConversationService(EducationConversationRepository conversationRepository,
                                       EducationMessageRepository messageRepository,
                                       RagProperties properties) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.properties = properties;
    }

    /**
     * Resolve an existing conversation by external id, scoped to the supplied
     * principal. Throws if the conversation does not belong to the principal.
     */
    @Transactional
    public EducationConversation resolveOrCreate(String externalConversationId, String principalName) {
        if (externalConversationId == null || externalConversationId.isBlank()) {
            return createConversation(principalName);
        }
        EducationConversation conversation = conversationRepository
                .findByExternalConversationId(externalConversationId)
                .orElseThrow(() -> new UnknownConversationException(
                        "Conversation not found: " + externalConversationId));
        if (!conversation.getPrincipalName().equals(principalName)) {
            throw new UnauthorizedConversationException(
                    "Conversation does not belong to the authenticated user");
        }
        return conversation;
    }

    @Transactional
    public EducationConversation createConversation(String principalName) {
        EducationConversation conversation = new EducationConversation();
        conversation.setExternalConversationId(UUID.randomUUID().toString());
        conversation.setPrincipalName(principalName);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public EducationMessage appendUserMessage(EducationConversation conversation, String text) {
        EducationMessage message = newMessage(conversation, "user", text);
        message.setGrounded(true); // user messages are always "grounded" trivially
        return messageRepository.save(message);
    }

    @Transactional
    public EducationMessage appendAssistantMessage(EducationConversation conversation,
                                                    RagAssistantPayload payload) {
        EducationMessage message = newMessage(conversation, "assistant", payload.getAnswer());
        message.setGrounded(payload.isGrounded());
        message.setRequiresProviderReview(payload.isRequiresProviderReview());
        message.setConfidence(payload.getConfidence());
        message.setCitationsJson(payload.getCitationsJson());
        return messageRepository.save(message);
    }

    /**
     * Returns the bounded, most recent prior turns (excluding the just-appended
     * user message) rendered as "User: ..." / "Assistant: ..." lines, capped by
     * the configured history window. Used to seed the LLM prompt.
     */
    public List<String> boundedHistory(EducationConversation conversation, EducationMessage exclude) {
        List<EducationMessage> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        int window = Math.max(0, properties.getConversation().getHistoryWindow());
        List<String> rendered = new ArrayList<>();
        List<EducationMessage> tail = messages.subList(Math.max(0, messages.size() - window), messages.size());
        for (EducationMessage msg : tail) {
            if (exclude != null && msg.getId() != null && msg.getId().equals(exclude.getId())) {
                continue;
            }
            rendered.add((("user".equals(msg.getRole())) ? "User" : "Assistant")
                    + ": " + truncate(msg.getMessageText(), 500));
        }
        return rendered;
    }

    public List<EducationMessage> history(EducationConversation conversation) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
    }

    public List<EducationConversation> conversationsFor(String principalName) {
        return conversationRepository.findByPrincipalNameOrderByUpdatedAtDesc(principalName);
    }

    private EducationMessage newMessage(EducationConversation conversation, String role, String text) {
        EducationMessage message = new EducationMessage();
        message.setConversation(conversation);
        message.setRole(role);
        message.setMessageText(text);
        message.setGrounded(true);
        message.setRequiresProviderReview(false);
        return message;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
