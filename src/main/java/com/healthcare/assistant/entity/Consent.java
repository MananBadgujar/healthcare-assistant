package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "consents")
@Data
public class Consent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String type;
    private Boolean active;

    public Consent() {}

    public Consent(Long patientId, String type, Boolean active) {
        // In real implementation, we would set patient reference.
        // Here we just store type and active; patient will be set on persist.
        this.type = type;
        this.active = active;
    }
}