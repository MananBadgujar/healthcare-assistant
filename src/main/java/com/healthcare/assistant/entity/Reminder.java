package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
@Data
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    private String message;
    private LocalDateTime scheduledFor;
    private LocalDateTime sentAt;
    private String status; // SENT, FAILED, IGNORED

    public Reminder() {}

    public Reminder(Patient patient, Provider provider, String message, LocalDateTime scheduledFor) {
        this.patient = patient;
        this.provider = provider;
        this.message = message;
        this.scheduledFor = scheduledFor;
    }

    public Reminder(Patient patient, String type, LocalDateTime scheduledFor) {
        this.patient = patient;
        this.message = type;
        this.scheduledFor = scheduledFor;
    }
}