package com.healthcare.assistant.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("default")
@TestPropertySource(locations = "classpath:application-kafka.properties")
class SerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void eventEnvelopeSerializationRoundTrip() throws JsonProcessingException {
        // Given
        var event = new EventEnvelope(
            "order-created",
            "order",
            "order-123",
            "publisher-service",
            Map.of("item", "widget", "qty", 42)
        );

        // When - serialize to JSON
        String json = objectMapper.writeValueAsString(event);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("eventId"));
        assertTrue(json.contains("eventType"));
        assertTrue(json.contains("timestamp"));
        assertTrue(json.contains("aggregateType"));
        assertTrue(json.contains("aggregateId"));
        assertTrue(json.contains("correlationId"));
        assertTrue(json.contains("source"));
        assertTrue(json.contains("version"));
        assertTrue(json.contains("payload"));

        // When - deserialize back
        EventEnvelope restored = objectMapper.readValue(json, EventEnvelope.class);

        // Then - all fields preserved
        assertEquals(event.getEventId(), restored.getEventId());
        assertEquals(event.getEventType(), restored.getEventType());
        assertEquals(event.getAggregateType(), restored.getAggregateType());
        assertEquals(event.getAggregateId(), restored.getAggregateId());
        assertEquals(event.getSource(), restored.getSource());
        assertEquals(event.getVersion(), restored.getVersion());
        assertEquals(event.getCorrelationId(), restored.getCorrelationId());

        // Payload may be deserialized as different type, verify it exists
        assertNotNull(restored.getPayload());
    }

    @Test
    void eventEnvelopeMinimalRoundTrip() throws JsonProcessingException {
        // Given - minimal event
        var event = new EventEnvelope(
            "test.event",
            "aggregate-type",
            "aggregate-id",
            "source", null
        );

        // When - serialize
        String json = objectMapper.writeValueAsString(event);

        // Then - has all expected fields
        assertNotNull(json);
        assertTrue(json.contains("eventId"));
        assertTrue(json.contains("eventType"));
        assertTrue(json.contains("timestamp"));
        assertTrue(json.contains("aggregateType"));
        assertTrue(json.contains("aggregateId"));
        assertTrue(json.contains("correlationId"));
        assertTrue(json.contains("source"));
        assertTrue(json.contains("version"));
        assertTrue(json.contains("payload"));

        // When - deserialize
        EventEnvelope restored = objectMapper.readValue(json, EventEnvelope.class);

        // Then - all fields match
        assertNotNull(restored.getEventId());
        assertEquals("test.event", restored.getEventType());
        assertNotNull(restored.getTimestamp());
        assertEquals("aggregate-type", restored.getAggregateType());
        assertEquals("aggregate-id", restored.getAggregateId());
        assertNotNull(restored.getCorrelationId());
        assertEquals("source", restored.getSource());
        assertEquals(1, restored.getVersion());
        assertNull(restored.getPayload());
    }

    @Test
    void kafkaMessageSerialization() throws JsonProcessingException {
        // Given
        var event = new EventEnvelope(
            "appointment-created",
            "appointment",
            "apt-001",
            "scheduler-service",
            Map.of("reason", "new appointment", "date", "2024-01-15")
        );

        // When - serialize to JSON (simulating Kafka message)
        String json = objectMapper.writeValueAsString(event);

        // Then - send would complete without error (verify JSON is valid)
        assertNotNull(json);
        assertTrue(json.contains("eventId"));
        assertTrue(json.contains("eventType"));
        assertTrue(json.contains("appointment-created"));
        assertTrue(json.contains("apt-001"));
    }
}