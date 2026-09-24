package com.healthcare.assistant.entity;

import com.healthcare.assistant.entity.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing")
@Data
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    private BigDecimal amount;
    private BigDecimal serviceAmount;
    private LocalDateTime billingDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private String billingType;
    private String description;
    private String currency;
    private LocalDate dateOfService;
    private LocalDate dueDate;
    private LocalDate issuedDate;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal adjustment;
    private BigDecimal totalAmount;

    public Billing() {}

    public Billing(BigDecimal amount, InvoiceStatus status) {
        this.amount = amount;
        this.status = status;
        this.billingDate = LocalDateTime.now();
    }
}