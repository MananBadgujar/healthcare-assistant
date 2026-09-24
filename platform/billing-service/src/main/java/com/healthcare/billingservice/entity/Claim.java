package com.healthcare.billingservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "claims")
public class Claim {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long invoiceId;
    private String insurer;
    private String status = "SUBMITTED";
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getInvoiceId() { return invoiceId; } public void setInvoiceId(Long v) { this.invoiceId = v; }
    public String getInsurer() { return insurer; } public void setInsurer(String v) { this.insurer = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
}
