package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "encounter")
@Data
public class Encounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    private LocalDateTime encounterDateTime;
    private String encounterType;
    private String reason;
    private String status; // COMPLETED, CANCELLED, SCHEDULED

    public Encounter() {}

    public Encounter(Patient patient, Provider provider, LocalDateTime encounterDateTime, String encounterType, String reason) {
        this.patient = patient;
        this.provider = provider;
        this.encounterDateTime = encounterDateTime;
        this.encounterType = encounterType;
        this.reason = reason;
    }
}