package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_allergy_cross_reactivities")
@Data
public class DrugAllergyCrossReactivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;

    @Column(name = "triggering_allergy", nullable = false)
    @NotBlank(message = "Triggering allergy must not be blank")
    private String triggeringAllergy;

    @Column(name = "cross_reactive_drugs", columnDefinition = "TEXT")
    private String crossReactiveDrugs;

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

    @Column(name = "detected_at", nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DrugAllergyCrossReactivity() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugAllergyCrossReactivity(Medication medication, String triggeringAllergy,
                                       String crossReactiveDrugs, String severity,
                                       String description, String recommendedAction,
                                       String evidence, String category) {
        this.medication = medication;
        this.triggeringAllergy = triggeringAllergy;
        this.crossReactiveDrugs = crossReactiveDrugs;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.evidence = evidence;
        this.category = category;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}