package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "refill")
@Data
public class Refill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    private int refillNumber;
    private LocalDateTime refillDate;
    private String status; // ACTIVE, COMPLETED, CANCELLED

    public Refill() {}

    public Refill(Prescription prescription, int refillNumber) {
        this.prescription = prescription;
        this.refillNumber = refillNumber;
        this.refillDate = LocalDateTime.now();
    }
}