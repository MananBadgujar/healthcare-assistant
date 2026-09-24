package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Stores feedback provided by a patient after an interaction.
 */
@Entity
@Table(name = "feedback")
@Data
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The patient who submitted the feedback.
     */
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    /**
     * The conversation associated with the feedback.
     */
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    /**
     * Patient's comment or rating.
     */
    private String comment;

    /**
     * Timestamp when the feedback was submitted.
     */
    private LocalDateTime createdAt;
}