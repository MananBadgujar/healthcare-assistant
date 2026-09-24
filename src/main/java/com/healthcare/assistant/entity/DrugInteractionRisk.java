package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_risks")
@Data
public class DrugInteractionRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String riskFactors;

    @Column(columnDefinition = "TEXT")
    private String monitoringRecommendations;

    @Column(name = "category", columnDefinition = "VARCHAR(255)")
    private String category;

    private boolean providerReviewed;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public DrugInteractionRisk() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionRisk(Medication medicationA, Medication medicationB,
                               RiskLevel riskLevel, String riskFactors,
                               String monitoringRecommendations, String category) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.riskLevel = riskLevel;
        this.riskFactors = riskFactors;
        this.monitoringRecommendations = monitoringRecommendations;
        this.category = category;
        this.providerReviewed = false;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public enum RiskLevel {
        LOW, MODERATE, HIGH, CRITICAL
    }
}