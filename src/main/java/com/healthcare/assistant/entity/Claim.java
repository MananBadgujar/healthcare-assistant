package com.healthcare.assistant.entity;

import com.healthcare.assistant.entity.Billing;
import com.healthcare.assistant.entity.enums.ClaimStatus;
import com.healthcare.assistant.entity.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "claims")
@Data
public class Claim {
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
    private ClaimStatus claimStatus;

    private Double amount;

    private String rejectionReason;

    private String claimNumber;
    private BigDecimal claimAmount;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Billing invoice;

    public Claim() {
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}