package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing a detected drug-drug, drug-condition, or drug-allergy interaction.
 * <p>
 * Interactions are detected deterministically via the ClinicalRuleEngine and
 * provide structured information for provider review. AI-assisted suggestions
 * must pass through safety validation before reaching the provider.
 */
@Entity
@Table(name = "drug_interactions")
@Data
public class DrugInteraction {

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
     * The interacting medication name (fallback for non-persisted).
     */
    @Column(name = "medication_b_name")
    private String medicationBName;

    /**
     * Severity level of the interaction.
     */
    @Column(name = "severity")
    private String severity;

    /**
     * Description of the interaction.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Recommended provider action.
     */
    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    /**
     * Evidence or source reference for the interaction.
     */
    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    /**
     * Clinical category of the interaction.
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

    public DrugInteraction() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

public DrugInteraction(Medication medicationA, Medication medicationB,
                       String severity, String description,
                       String recommendedAction, String category) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.medicationBName = medicationB != null ? medicationB.getName() : null;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.category = category;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}