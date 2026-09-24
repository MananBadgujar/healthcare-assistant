package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing aggregated drug interaction severity data.
 * <p>
 * Aggregates interaction counts by severity level between two medications,
 * providing a summary view for provider review and clinical decision support.
 */
@Entity
@Table(name = "drug_interaction_aggregations")
@Data
public class DrugInteractionAggregation {

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
     * Total number of interactions detected between the two medications.
     */
    @Column(name = "total_interactions")
    private Integer totalInteractions;

    /**
     * Number of CRITICAL severity interactions.
     */
    @Column(name = "severe_interactions")
    private Integer severeInteractions;

    /**
     * Number of HIGH severity interactions.
     */
    @Column(name = "moderate_interactions")
    private Integer moderateInteractions;

    /**
     * Number of MODERATE severity interactions.
     */
    @Column(name = "mild_interactions")
    private Integer mildInteractions;

    /**
     * Clinical categories of interactions detected.
     */
    @Column(name = "interaction_categories", columnDefinition = "TEXT")
    private String interactionCategories;

    /**
     * Overall severity level of all interactions combined.
     */
    @Column(name = "overall_severity")
    private String overallSeverity;

    /**
     * Whether this aggregation has been reviewed by a provider.
     */
    private boolean providerReviewed = false;

    /**
     * Timestamp when the aggregation was detected.
     */
    private LocalDateTime detectedAt;

    /**
     * Timestamp when the aggregation was last updated.
     */
    private LocalDateTime updatedAt;

    public DrugInteractionAggregation() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionAggregation(Medication medicationA, Medication medicationB) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}