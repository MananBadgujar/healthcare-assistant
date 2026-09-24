package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents the result of a triage operation.
 */
@Entity
@Table(name = "triage_records")
@Data
public class TriageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The patient who underwent triage.
     */
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    /**
     * The conversation associated with this triage.
     */
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    /**
     * The urgency level indicated by the triage.
     */
    @Enumerated(EnumType.STRING)
    private Urgency urgency;

    /**
     * Brief summary of the triage outcome.
     */
    private String summary;

    /**
     * Timestamp when the triage record was created.
     */
    private LocalDateTime createdAt;
}