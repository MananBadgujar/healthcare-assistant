package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_audits")
@Data
public class DrugInteractionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Column(name = "audit_type", nullable = false)
    private String auditType;

    @Column(name = "audit_findings", columnDefinition = "TEXT")
    private String auditFindings;

    @Column(name = "compliance_status")
    private String complianceStatus;

    @Column(name = "provider_reviewed")
    private boolean providerReviewed;

    @Column(name = "audit_date", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DrugInteractionAudit() {
        this.auditDate = LocalDateTime.now();
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DrugInteractionAudit(Medication medicationA, Medication medicationB,
                                String auditType, String auditFindings,
                                String complianceStatus, boolean providerReviewed) {
        this.medicationA = medicationA;
        this.medicationB = medicationB;
        this.auditType = auditType;
        this.auditFindings = auditFindings;
        this.complianceStatus = complianceStatus;
        this.providerReviewed = providerReviewed;
        this.auditDate = LocalDateTime.now();
        this.detectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}