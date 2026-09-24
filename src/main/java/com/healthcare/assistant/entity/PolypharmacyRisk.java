package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "polypharmacy_risks")
@Data
public class PolypharmacyRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "medication_count", nullable = false)
    private int medicationCount;

    @Column(name = "severe_interaction_count", nullable = false)
    private int severeInteractionCount;

    @Column(name = "moderate_interaction_count", nullable = false)
    private int moderateInteractionCount;

    @Column(name = "mild_interaction_count", nullable = false)
    private int mildInteractionCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String riskFactors;

    @Column(columnDefinition = "TEXT")
    private String clinicalRecommendations;

    private boolean providerReviewed;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public PolypharmacyRisk() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PolypharmacyRisk(Patient patient, int medicationCount,
                            int severeInteractionCount, int moderateInteractionCount,
                            int mildInteractionCount, RiskLevel riskLevel) {
        this.patient = patient;
        this.medicationCount = medicationCount;
        this.severeInteractionCount = severeInteractionCount;
        this.moderateInteractionCount = moderateInteractionCount;
        this.mildInteractionCount = mildInteractionCount;
        this.riskLevel = riskLevel;
        this.providerReviewed = false;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public enum RiskLevel {
        LOW, MODERATE, HIGH, CRITICAL
    }
}