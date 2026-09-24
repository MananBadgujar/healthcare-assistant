package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing the resolution of a drug-drug interaction.
 * <p>
 * Records provider-reviewed resolution actions taken for detected
 * drug interactions including dosage adjustments, medication switches,
 * monitoring plans, and discontinuations.
 */
@Entity
@Table(name = "drug_interaction_resolutions")
@Data
public class DrugInteractionResolution {

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
     * Resolution status of the interaction.
     * PENDING: awaiting provider review
     * IN_PROGRESS: resolution action in progress
     * RESOLVED: resolution completed
     */
    @Column(name = "resolution_status")
    private String resolutionStatus;

    /**
     * Date when the resolution was recorded.
     */
    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    /**
     * Provider who resolved the interaction.
     */
    @Column(name = "resolved_by")
    private String resolvedBy;

    /**
     * Notes documenting the resolution decision.
     */
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    /**
     * Action taken to resolve the interaction.
     */
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    /**
     * Category of resolution action taken.
     * DOSE_ADJUSTMENT: dosage modified
     * MEDICATION_SWITCH: different medication prescribed
     * MONITORING: additional monitoring planned
     * DISCONTINUATION: medication stopped
     */
    @Column(name = "resolution_category")
    private String resolutionCategory;

    /**
     * Whether this resolution has been reviewed by a provider.
     */
    private boolean providerReviewed = false;

    /**
     * Timestamp when the interaction was originally detected.
     */
    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    /**
     * Timestamp when the resolution record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DrugInteractionResolution() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionResolution(Medication medicationA, Medication medicationB,
                                      String resolutionStatus, LocalDateTime resolutionDate,
                                      String resolvedBy, String resolutionNotes,
                                      String actionTaken, String resolutionCategory) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.resolutionStatus = resolutionStatus;
        this.resolutionDate = resolutionDate != null ? resolutionDate : LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = resolutionNotes;
        this.actionTaken = actionTaken;
        this.resolutionCategory = resolutionCategory;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}