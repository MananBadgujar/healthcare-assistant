package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conversation between a patient and the AI assistant.
 */
@Entity
@Table(name = "ai_conversations")
@Data
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The patient who initiated the conversation.
     */
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    /**
     * External conversation identifier for session tracking.
     */
    private String externalConversationId;

    /**
     * Creation timestamp of the conversation.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp of the conversation.
     */
    private LocalDateTime updatedAt;

    /**
     * Current status of the conversation.
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * List of messages in this conversation.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationMessage> messages = new ArrayList<>();

    /**
     * Possible statuses for a conversation.
     */
    public enum Status {
        NEW,
        IN_PROGRESS,
        COMPLETED,
        TERMINATED
    }
}