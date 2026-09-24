package com.healthcare.assistant.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "prescription")
@Data
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;

    private String dosage;
    private LocalDateTime prescribedAt;
    private String status; // ACTIVE, COMPLETED, CANCELLED

    public Prescription() {}

    public Prescription(Patient patient, Provider provider, Medication medication, String dosage) {
        this.patient = patient;
        this.provider = provider;
        this.medication = medication;
        this.dosage = dosage;
        this.prescribedAt = LocalDateTime.now();
    }

    public Prescription(Patient patient, Medication medication, String dosage, String frequency, String reason) {
        this.patient = patient;
        this.medication = medication;
        this.dosage = dosage;
        // frequency and reason are placeholders for future domain attributes
    }

    public Prescription(Patient patient, Medication medication, String dosage, String frequency, String reason, LocalDate startDate, LocalDate endDate) {
        this.patient = patient;
        this.medication = medication;
        this.dosage = dosage;
        // ignore frequency, reason, startDate, endDate for now
    }
}