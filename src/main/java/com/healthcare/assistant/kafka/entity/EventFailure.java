package com.healthcare.assistant.kafka.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "event_failures")
@Data
public class EventFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 36)
    private String aggregateId;

    @Column(nullable = false, length = 36)
    private String correlationId;

    @Column(nullable = false, length = 50)
    private String sourceTopic;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String exceptionDetails;

    @Column(nullable = false)
    private Instant failureTimestamp;

    @Column(nullable = false, length = 50)
    private String dlqStatus; // PENDING, SENT_TO_DLQ, PROCESSED

    public EventFailure() {
    }

    public EventFailure(String eventId, String eventType, String aggregateId,
                        String correlationId, String sourceTopic, Integer attemptCount,
                        String failureReason, String exceptionDetails) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.sourceTopic = sourceTopic;
        this.attemptCount = attemptCount;
        this.failureReason = failureReason;
        this.exceptionDetails = exceptionDetails;
        this.failureTimestamp = Instant.now();
        this.dlqStatus = "PENDING";
    }
}