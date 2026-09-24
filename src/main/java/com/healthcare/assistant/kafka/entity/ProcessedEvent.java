package com.healthcare.assistant.kafka.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "processed_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"eventId", "consumerId"})
})
@Data
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String eventId;

    @Column(nullable = false, length = 36)
    private String aggregateId;

    @Column(nullable = false, length = 36)
    private String consumerId;

    @Column(nullable = false)
    private Instant processedAt;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 200)
    private String errorMessage;

    public ProcessedEvent() {
    }

    public ProcessedEvent(String eventId, String aggregateId, String consumerId,
                          String status, Instant processedAt) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.consumerId = consumerId;
        this.status = status;
        this.processedAt = processedAt;
    }
}