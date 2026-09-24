package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.healthcare.assistant.entity.enums.ContraindicationSeverity;
import com.healthcare.assistant.entity.enums.ContraindicationType;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_contraindications")
@Data
public class DrugInteractionContraindication {

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
    @Column(name = "contraindication_type", nullable = false)
    private ContraindicationType contraindicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "contraindication_severity", nullable = false)
    private ContraindicationSeverity contraindicationSeverity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "category")
    private String category;

    private boolean providerReviewed = false;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public DrugInteractionContraindication() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionContraindication(Medication medicationA, Medication medicationB,
                                           ContraindicationType contraindicationType,
                                           ContraindicationSeverity contraindicationSeverity,
                                           String description, String recommendedAction,
                                           String evidence, String category) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.contraindicationType = contraindicationType;
        this.contraindicationSeverity = contraindicationSeverity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.evidence = evidence;
        this.category = category;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}