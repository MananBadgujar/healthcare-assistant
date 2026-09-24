package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.healthcare.assistant.entity.enums.TestType;
import com.healthcare.assistant.entity.enums.TestResult;
import com.healthcare.assistant.entity.enums.ValidationStatus;
import java.time.LocalDateTime;

/**
 * Entity representing a drug interaction test validation record.
 * <p>
 * Tracks drug interaction testing events including test type, results,
 * validation status, and provider review information.
 */
@Entity
@Table(name = "drug_interaction_testing")
@Data
public class DrugInteractionTesting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;

    @Column(name = "test_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TestType testType;

    @Column(name = "test_result")
    @Enumerated(EnumType.STRING)
    private TestResult testResult;

    @Column(name = "test_details", columnDefinition = "TEXT")
    private String testDetails;

    @Column(name = "validated_by", length = 100)
    private String validatedBy;

    @Column(name = "validation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;

    @Column(name = "evidence_reference", columnDefinition = "TEXT")
    private String evidenceReference;

    @Column(name = "provider_reviewed")
    private boolean providerReviewed = false;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DrugInteractionTesting() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionTesting(Medication medicationA, Medication medicationB,
                                   TestType testType, LocalDateTime testDate,
                                   TestResult testResult, String testDetails,
                                   String validatedBy, ValidationStatus validationStatus,
                                   String evidenceReference) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.testType = testType;
        this.testDate = testDate != null ? testDate : LocalDateTime.now();
        this.testResult = testResult;
        this.testDetails = testDetails;
        this.validatedBy = validatedBy;
        this.validationStatus = validationStatus;
        this.evidenceReference = evidenceReference;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}