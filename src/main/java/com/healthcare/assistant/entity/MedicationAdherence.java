package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_adherence")
@Data
public class MedicationAdherence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;

    private double adherencePercentage;

    private LocalDateTime lastTaken;

    private int missedDoses;

    private String dosageInstructions;

    private Boolean providerReviewed;

    private LocalDateTime detectedAt;

    private LocalDateTime updatedAt;

    public MedicationAdherence() {}
}