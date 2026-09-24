package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_plans")
@Data
public class CarePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String plan;

    private LocalDateTime createdAt;

    public CarePlan() {}

    public CarePlan(String plan) {
        this.plan = plan;
        this.createdAt = LocalDateTime.now();
    }

    public CarePlan(Long patientId, String plan, String createdAt) {
        this.plan = plan;
        this.createdAt = LocalDateTime.parse(createdAt);
    }
}