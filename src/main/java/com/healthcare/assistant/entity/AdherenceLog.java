package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "adherence_logs")
@Data
public class AdherenceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;

    private LocalDateTime timestamp;

    private Boolean taken;

    private String notes;

    public AdherenceLog() {}

    public AdherenceLog(Patient patient, Medication medication, LocalDateTime timestamp, Boolean taken, String notes) {
        this.patient = patient;
        this.medication = medication;
        this.timestamp = timestamp;
        this.taken = taken;
        this.notes = notes;
    }
}