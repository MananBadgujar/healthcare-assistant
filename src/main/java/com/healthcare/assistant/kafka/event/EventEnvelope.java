package com.healthcare.assistant.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope {

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String aggregateType;
    private String aggregateId;
    private String correlationId;
    private String source;
    private int version;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Object payload;

    public EventEnvelope(String eventType, String aggregateType, String aggregateId,
                         String source, Object payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.correlationId = UUID.randomUUID().toString();
        this.source = source;
        this.version = 1;
        this.payload = payload;
    }

    public static EventEnvelope of(String eventType, String aggregateType, String aggregateId,
                                   String source, Object payload) {
        return new EventEnvelope(eventType, aggregateType, aggregateId, source, payload);
    }
}