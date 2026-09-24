package com.healthcare.assistant.kafka.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "event_records")
@Data
public class EventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false, length = 36)
    private String aggregateId;

    @Column(nullable = false, length = 36)
    private String correlationId;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 50)
    private String processingStatus;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(length = 200)
    private String errorDetails;

    public EventRecord() {
    }

    public EventRecord(String eventId, String eventType, String aggregateType,
                       String aggregateId, String correlationId, String source,
                       Object payload, Instant timestamp, Integer version) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.source = source;
        this.payload = payload != null ? payload.toString() : null;
        this.timestamp = timestamp;
        this.version = version;
        this.processingStatus = "PENDING";
        this.retryCount = 0;
    }
}