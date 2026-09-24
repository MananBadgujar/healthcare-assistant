package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_documentation")
@Data
public class DrugInteractionDocumentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Column(name = "documented_by")
    private String documentedBy;

    @Column(name = "documentation_date")
    private LocalDateTime documentationDate;

    @Column(name = "interaction_summary", columnDefinition = "TEXT")
    private String interactionSummary;

    @Column(name = "evidence_level")
    private String evidenceLevel;

    @Column(name = "source_reference", columnDefinition = "TEXT")
    private String sourceReference;

    @Column(name = "provider_reviewed")
    private boolean providerReviewed;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "category")
    private String category;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public DrugInteractionDocumentation() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionDocumentation(Medication medicationA, Medication medicationB,
                                        String documentedBy, LocalDateTime documentationDate,
                                        String interactionSummary, String evidenceLevel,
                                        String sourceReference, String category) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.documentedBy = documentedBy;
        this.documentationDate = documentationDate;
        this.interactionSummary = interactionSummary;
        this.evidenceLevel = evidenceLevel;
        this.sourceReference = sourceReference;
        this.category = category;
        this.providerReviewed = false;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}