package com.healthcare.billingservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private Double amount;
    private String status = "PENDING";
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; } public void setPatientId(Long v) { this.patientId = v; }
    public Double getAmount() { return amount; } public void setAmount(Double v) { this.amount = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
}
