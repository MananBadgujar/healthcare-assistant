package com.healthcare.assistant.kafka.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "event_audit_log")
@Data
public class EventAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 36)
    private String correlationId;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(columnDefinition = "text")
    private String details;

    public EventAuditLog() {
    }

    public EventAuditLog(String eventId, String eventType, String correlationId,
                         String source, Instant timestamp, String status, String details) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.source = source;
        this.timestamp = timestamp;
        this.status = status;
        this.details = details;
    }
}