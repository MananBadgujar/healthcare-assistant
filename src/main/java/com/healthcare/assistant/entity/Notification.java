package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(length = 2000)
    private String message;

    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String status; // could be READ, UNREAD, SENT, FAILED

    public Notification() {}

    public Notification(Patient patient, String message) {
        this.patient = patient;
        this.message = message;
        this.sentAt = LocalDateTime.now();
        this.status = "UNREAD";
    }
}