package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.healthcare.assistant.entity.enums.ContraindicationSeverity;

/**
 * Entity representing a clinical contraindication.
 * <p>
 * Contraindications are detected deterministically via clinical rules
 * and provide structured information for provider review.
 * AI-assisted contraindication suggestions must pass through safety
 * validation before reaching the provider.
 */
@Entity
@Table(name = "contraindications")
public class Contraindication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The patient this contraindication applies to.
     */
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * The medication or treatment affected (name stored as string).
     */
    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    /**
     * The medical condition triggering the contraindication (name stored as string).
     */
    @Column(name = "condition_name", nullable = false)
    private String conditionName;

    /**
     * The allergy triggering the contraindication (name stored as string).
     */
    @Column(name = "allergy_name")
    private String allergyName;

    /**
     * Severity level of the contraindication.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private ContraindicationSeverity severity;

    /**
     * Description of the contraindication.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Recommended action.
     */
    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    /**
     * Evidence or guideline reference.
     */
    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    /**
     * Whether this has been reviewed by a provider.
     */
    private boolean providerReviewed = false;

    /**
     * Timestamp when the contraindication was detected.
     */
    private LocalDateTime detectedAt;

    /**
     * Timestamp when the contraindication was last updated.
     */
    private LocalDateTime updatedAt;

    public Contraindication() {
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Contraindication(Patient patient, String medicationName, String conditionName,
                        ContraindicationSeverity severity, String description,
                        String recommendedAction, String evidence) {
        this.patient = patient;
        this.medicationName = medicationName;
        this.conditionName = conditionName;
        this.allergyName = null;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.evidence = evidence;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Contraindication(Patient patient, String medicationName, String conditionName,
                            String allergyName,
                            ContraindicationSeverity severity, String description,
                            String recommendedAction, String evidence) {
        this.patient = patient;
        this.medicationName = medicationName;
        this.conditionName = conditionName;
        this.allergyName = allergyName;
        this.severity = severity;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.evidence = evidence;
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getAllergyName() {
        return allergyName;
    }

    public void setAllergyName(String allergyName) {
        this.allergyName = allergyName;
    }

    public ContraindicationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ContraindicationSeverity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public boolean isProviderReviewed() {
        return providerReviewed;
    }

    public void setProviderReviewed(boolean providerReviewed) {
        this.providerReviewed = providerReviewed;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}