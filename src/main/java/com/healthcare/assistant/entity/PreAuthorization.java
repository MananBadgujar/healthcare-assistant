package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.healthcare.assistant.entity.enums.PreAuthorizationStatus;

@Entity
@Table(name = "pre_authorization")
@Data
public class PreAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "insurance_id")
    private Insurance insurance;

    @Enumerated(EnumType.STRING)
    private PreAuthorizationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String reason;

    private String medicalInformation;
    private String serviceInformation;
    private BigDecimal requestedAmount;
    private String rejectionReason;

    public PreAuthorization() {}

    public PreAuthorization(Patient patient, Insurance insurance, PreAuthorizationStatus status) {
        this.patient = patient;
        this.insurance = insurance;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}