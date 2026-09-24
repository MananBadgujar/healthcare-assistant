package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a single message within a conversation.
 */
@Entity
@Table(name = "conversation_messages")
@Data
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The conversation this message belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    /**
     * The textual content of the message.
     */
    private String message;

    /**
     * Timestamp when the message was sent.
     */
    private LocalDateTime timestamp;

    /**
     * Indicates whether the message originated from the patient (true) or the assistant (false).
     */
    private boolean isUser;
}