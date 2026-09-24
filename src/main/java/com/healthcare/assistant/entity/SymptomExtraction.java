package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity that captures extracted symptom information from a patient message.
 */
@Entity
@Table(name = "symptom_extractions")
@Data
public class SymptomExtraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The raw message from the patient.
     */
    private String originalMessage;

    /**
     * List of identified symptoms.
     */
    @ElementCollection
    @Column(name = "symptom")
    private List<String> symptoms = new ArrayList<>();

    /**
     * Duration of symptoms described by the patient.
     */
    private String duration;

    /**
     * Severity level reported by the patient.
     */
    private String severity;

    /**
     * Medication mentioned by the patient.
     */
    private String medication;

    /**
     * Allergy or drug reaction mentioned.
     */
    private String allergy;

    /**
     * Specific medical condition mentioned.
     */
    private String medicalCondition;

    /**
     * Timestamp when extraction was performed.
     */
    private LocalDateTime createdAt;

    // Constructors
    public SymptomExtraction() {
    }

    public SymptomExtraction(String originalMessage) {
        this.originalMessage = originalMessage;
        this.createdAt = LocalDateTime.now();
    }

    public SymptomExtraction(String originalMessage, List<String> symptoms, String duration, String severity, String medication, String allergy, String medicalCondition, String createdAt) {
        this.originalMessage = originalMessage;
        this.symptoms = symptoms;
        this.duration = duration;
        this.severity = severity;
        this.medication = medication;
        this.allergy = allergy;
        this.medicalCondition = medicalCondition;
        this.createdAt = LocalDateTime.parse(createdAt);
    }
}