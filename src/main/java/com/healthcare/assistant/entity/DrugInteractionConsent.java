package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_consents")
@Data
public class DrugInteractionConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    private Boolean consentGiven;

    private LocalDateTime consentDate;

    private String consentBy;

    private String consentScope;

    private Boolean sideEffectsDiscussed;

    private String monitoringPlan;

    private Boolean providerReviewed;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public DrugInteractionConsent() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionConsent(Medication medicationA, Medication medicationB,
                                  Boolean consentGiven, LocalDateTime consentDate,
                                  String consentBy, String consentScope,
                                  Boolean sideEffectsDiscussed, String monitoringPlan,
                                  Boolean providerReviewed) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.consentGiven = consentGiven;
        this.consentDate = consentDate;
        this.consentBy = consentBy;
        this.consentScope = consentScope;
        this.sideEffectsDiscussed = sideEffectsDiscussed;
        this.monitoringPlan = monitoringPlan;
        this.providerReviewed = providerReviewed;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}