package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_alerts")
@Data
public class DrugInteractionAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Column(name = "alert_level", nullable = false)
    private String alertLevel;

    @Column(name = "alert_text", columnDefinition = "TEXT")
    private String alertText;

    @Column(name = "action_required", columnDefinition = "TEXT")
    private String actionRequired;

    @Column(name = "category", columnDefinition = "TEXT")
    private String category;

    private boolean providerReviewed;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public DrugInteractionAlert() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionAlert(Medication medicationA, Medication medicationB,
                                String alertLevel, String alertText,
                                String actionRequired, String category) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.alertLevel = alertLevel;
        this.alertText = alertText;
        this.actionRequired = actionRequired;
        this.category = category;
        this.providerReviewed = false;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}