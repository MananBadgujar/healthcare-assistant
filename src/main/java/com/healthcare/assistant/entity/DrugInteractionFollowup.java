package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_followups")
@Data
public class DrugInteractionFollowup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Column(name = "followup_date", nullable = false)
    private LocalDateTime followupDate;

    @Column(name = "followup_type", nullable = false)
    private String followupType;

    @Column(name = "followup_status")
    private String followupStatus;

    @Column(name = "followup_notes", columnDefinition = "TEXT")
    private String followupNotes;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "provider_reviewed")
    private boolean providerReviewed;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DrugInteractionFollowup() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionFollowup(Medication medicationA, Medication medicationB,
                                   LocalDateTime followupDate, String followupType,
                                   String followupStatus) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.followupDate = followupDate;
        this.followupType = followupType;
        this.followupStatus = followupStatus;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}