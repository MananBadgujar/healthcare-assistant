package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity representing a maximum daily dose check record.
 * <p>
 * Tracks medications where the current daily dose exceeds the maximum recommended dose,
 * with severity assessment and provider action recommendations.
 */
@Entity
@Table(name = "max_daily_dose")
@Data
public class MaxDailyDose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;

    @Column(name = "max_daily_dose")
    private String maxDailyDose;

    @Column(name = "current_daily_dose")
    private String currentDailyDose;

    @Column(name = "severity")
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "category")
    private String category;

    @Column(name = "provider_reviewed")
    private boolean providerReviewed = false;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MaxDailyDose() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public MaxDailyDose(Medication medication, String maxDailyDose, String currentDailyDose,
                        String severity, String description, String recommendedAction,
                        String evidence, String category) {
        this.medication = medication;
        this.maxDailyDose = maxDailyDose;
        this.currentDailyDose = currentDailyDose;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.evidence = evidence;
        this.category = category;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}