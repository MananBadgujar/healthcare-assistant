package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import com.healthcare.assistant.entity.enums.InvoiceStatus;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    public Invoice() {}

    public Invoice(String id, InvoiceStatus status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
}