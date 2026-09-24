package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversation entity for conversational patient-education (RAG) sessions.
 * <p>
 * Each conversation groups the back-and-forth between a patient and the
 * assistant, keeps the authenticated principal reference, and owns its
 * messages. Message history is bounded by the configured context window so the
 * full history is never sent to the LLM.
 */
@Entity
@Table(name = "education_conversations")
@Data
public class EducationConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * External conversation identifier surfaced to clients for multi-turn
     * follow-up. Unique across conversations.
     */
    @Column(name = "external_conversation_id", nullable = false, unique = true)
    private String externalConversationId;

    /** Authenticated principal name (user email) owning the conversation. */
    @Column(name = "principal_name", nullable = false)
    private String principalName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<EducationMessage> messages = new ArrayList<>();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
