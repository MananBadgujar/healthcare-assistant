package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_severity_scores")
@Data
public class DrugInteractionSeverityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    /**
     * Severity score from 0-10, where 0 is no interaction and 10 is life-threatening.
     */
    @Column(name = "severity_score")
    private Integer severityScore;

    /**
     * Risk factors associated with the interaction.
     */
    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    /**
     * Clinical guidance for the provider.
     */
    @Column(name = "clinical_guidance", columnDefinition = "TEXT")
    private String clinicalGuidance;

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
     * Timestamp when the severity score was detected.
     */
    private LocalDateTime detectedAt;

    /**
     * Timestamp when the severity score was last updated.
     */
    private LocalDateTime updatedAt;

    public DrugInteractionSeverityScore() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionSeverityScore(Medication medicationA, Medication medicationB,
                                        Integer severityScore, String riskFactors,
                                        String clinicalGuidance, String category) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.severityScore = severityScore;
        this.riskFactors = riskFactors;
        this.clinicalGuidance = clinicalGuidance;
        this.category = category;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}