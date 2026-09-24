package com.healthcare.contracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Versioned Kafka domain-event envelope shared by all services. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainEvent {
    private String eventId;
    private String eventName;
    private String eventVersion = "v1";
    private Instant occurredAt;
    private String correlationId;
    private String entityType;
    private String entityId;
    private String producer;
    private Map<String, Object> payload;

    public DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    public static DomainEvent of(String eventName, String entityType, String entityId,
                                 String producer, String correlationId, Map<String, Object> payload) {
        DomainEvent e = new DomainEvent();
        e.eventName = eventName;
        e.entityType = entityType;
        e.entityId = entityId;
        e.producer = producer;
        e.correlationId = correlationId;
        e.payload = payload;
        return e;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventVersion() { return eventVersion; }
    public void setEventVersion(String eventVersion) { this.eventVersion = eventVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
