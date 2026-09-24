package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "telehealth_sessions")
@Data
public class TelehealthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    private LocalDateTime scheduledStart;

    private LocalDateTime scheduledEnd;

    private LocalDateTime actualStart;

    private LocalDateTime actualEnd;

    private String status; // SCHEDULED, READY, IN_PROGRESS, COMPLETED, CLOSED, CANCELLED, NO_SHOW

    private String meetingUrl;

    private String sessionReference;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TelehealthSession() {
    }

    public TelehealthSession(Patient patient, Provider provider, LocalDateTime scheduledStart, LocalDateTime scheduledEnd) {
        this.patient = patient;
        this.provider = provider;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}