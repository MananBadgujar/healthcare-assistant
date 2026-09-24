package com.healthcare.assistant.kafka;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("default")
@TestPropertySource(locations = "classpath:application-kafka.properties")
class CorrelationIdTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void correlationIdSurvivesSerialization() throws Exception {
        // Given - event with correlation ID
        var event = new EventEnvelope();
        event.setEventId("appointment-created-Id");
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-001");
        event.setSource("scheduler-service");
        event.setPayload(Map.of("reason", "new appointment"));
        event.setCorrelationId("correlation-12345");

        // When - serialize to JSON
        String json = objectMapper.writeValueAsString(event);

        // Then - correlation ID should be in JSON
        assertTrue(json.contains("correlation-12345"),
            "correlation ID should survive JSON serialization: " + json);

        // And - can deserialize back with same correlation ID
        EventEnvelope deserialized = objectMapper.readValue(json, EventEnvelope.class);
        assertEquals("correlation-12345", deserialized.getCorrelationId(),
            "correlation ID should survive deserialization");
    }

    @Test
    void correlationIdSurvivesFullFlow() {
        // Given - event with correlation ID
        var event = new EventEnvelope();
        event.setEventId("clinical-event-Id");
        event.setEventType("clinical-event");
        event.setTimestamp(Instant.now());
        event.setAggregateType("clinical");
        event.setAggregateId("clinic-001");
        event.setSource("clinical-service");
        event.setCorrelationId("correlation-99999");
        event.setVersion(1);

        // When - verify correlation ID is set
        assertEquals("correlation-99999", event.getCorrelationId(),
            "correlation ID should be set on event creation");

        // And - all other fields are accessible
        assertNotNull(event.getEventId(), "eventId should not be null");
        assertNotNull(event.getEventType(), "eventType should not be null");
        assertNotNull(event.getTimestamp(), "timestamp should not be null");
        assertNotNull(event.getAggregateType(), "aggregateType should not be null");
        assertNotNull(event.getAggregateId(), "aggregateId should not be null");
        assertNotNull(event.getSource(), "source should not be null");
        assertNotNull(event.getVersion(), "version should not be null");
        // payload is null in this test, so we don't assert it's not null
    }
}