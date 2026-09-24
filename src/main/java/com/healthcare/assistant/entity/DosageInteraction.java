package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing a detected dosage interaction between medications.
 * <p>
 * Dosage interactions include duplicate dosing, dosing range conflicts,
 * and other dosage-related concerns that require provider review.
 */
@Entity
@Table(name = "dosage_interactions")
@Data
public class DosageInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The primary medication (trigger).
     */
    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    /**
     * The interacting medication.
     */
    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    /**
     * Severity level of the dosage interaction.
     */
    @Column(name = "severity")
    private String severity;

    /**
     * Description of the dosage interaction.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Recommended provider action for the dosage interaction.
     */
    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    /**
     * Evidence or source reference for the dosage interaction.
     */
    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    /**
     * Clinical category of the dosage interaction.
     */
    @Column(name = "category")
    private String category;

    /**
     * Whether this interaction has been reviewed by a provider.
     */
    private boolean providerReviewed = false;

    /**
     * Timestamp when the interaction was detected.
     */
    private LocalDateTime detectedAt;

    /**
     * Timestamp when the interaction was last updated.
     */
    private LocalDateTime updatedAt;

    public DosageInteraction() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DosageInteraction(Medication medicationA, Medication medicationB,
                             String severity, String description,
                             String recommendedAction, String category,
                             String evidence) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.category = category;
        this.evidence = evidence;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}