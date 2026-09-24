package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "medications")
@Data
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Dosage must not be blank")
    private String dosage;

    @NotBlank(message = "Frequency must not be blank")
    private String frequency;

    @NotBlank(message = "Instructions must not be blank")
    private String instructions;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private LocalDateTime createdAt;

    public Medication() {}

    public Medication(String name, String dosage, String frequency, String instructions, Patient patient) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.instructions = instructions;
        this.patient = patient;
        this.createdAt = LocalDateTime.now();
    }
}