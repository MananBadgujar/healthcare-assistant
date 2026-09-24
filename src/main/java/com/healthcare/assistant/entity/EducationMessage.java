package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single message in an {@link EducationConversation}.
 * <p>
 * User messages and assistant messages share this entity, distinguished by the
 * {@code role} field. Assistant messages additionally store the citations
 * (source references) returned with the answer and a serialised representation
 * of the retrieved evidence used to ground the answer, which is later used by
 * the citation validator and feedback tracking.
 */
@Entity
@Table(name = "education_messages")
@Data
public class EducationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private EducationConversation conversation;

    /** "user" or "assistant". */
    @Column(nullable = false)
    private String role;

    @Column(name = "message_text", nullable = false)
    @Lob
    private String messageText;

    /**
     * JSON-serialised list of citations returned with the answer (assistant
     * messages only). Stored as text so the message table stays portable.
     */
    @Column(name = "citations_json")
    @Lob
    private String citationsJson;

    /** Whether the answer was determined to be grounded in retrieved evidence. */
    @Column(nullable = false)
    private boolean grounded;

    @Column(name = "requires_provider_review", nullable = false)
    private boolean requiresProviderReview;

    @Column(name = "confidence")
    private String confidence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EducationFeedback> feedback = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
