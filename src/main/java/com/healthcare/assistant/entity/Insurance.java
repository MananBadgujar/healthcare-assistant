package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "insurances")
@Data
public class Insurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String policyNumber;
    private String provider;
    private String status;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
private String providerName;
private String memberId;
private String planName;

    public Insurance() {}

    public Insurance(String policyNumber, String provider, String status) {
        this.policyNumber = policyNumber;
        this.provider = provider;
        this.status = status;
        this.effectiveFrom = LocalDateTime.now();
    }
}