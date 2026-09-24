package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Feedback submitted against an {@link EducationMessage}.
 * <p>
 * Captures the feedback category (helpful / not helpful / incorrect information
 * / missing information / citation issue) plus an optional free-text comment
 * and the authenticated user who submitted it. No sensitive patient data is
 * stored beyond the minimum needed to associate the feedback with the message.
 */
@Entity
@Table(name = "education_feedback")
@Data
public class EducationFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private EducationMessage message;

    /** {@link FeedbackCategory} name. */
    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "comment")
    @Lob
    private String comment;

    /** Authenticated principal who submitted the feedback. */
    @Column(name = "principal_name", nullable = false)
    private String principalName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** Fixed feedback categories supported by the patient-education flow. */
    public enum FeedbackCategory {
        HELPFUL,
        NOT_HELPFUL,
        INCORRECT_INFORMATION,
        MISSING_INFORMATION,
        CITATION_ISSUE
    }
}
