package com.healthcare.assistant.kafka;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("default")
@TestPropertySource(locations = "classpath:application-kafka.properties")
class VersioningTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void eventVersioning1() {
        // Given - v1 event created via constructor
        var event = new EventEnvelope(
            "appointment-created",
            "appointment",
            "apt-001",
            "scheduler-service",
            Map.of("reason", "new appointment")
        );

        // Then - version should default to 1
        assertEquals(1, event.getVersion(),
            "v1 event version should default to 1, got: " + event.getVersion());

        // And - all other fields are accessible
        assertNotNull(event.getEventId(), "eventId should not be null");
        assertNotNull(event.getEventType(), "eventType should not be null");
        assertNotNull(event.getTimestamp(), "timestamp should not be null");
        assertNotNull(event.getAggregateType(), "aggregateType should not be null");
        assertNotNull(event.getAggregateId(), "aggregateId should not be null");
        assertNotNull(event.getSource(), "source should not be null");
        assertNotNull(event.getCorrelationId(), "correlationId should not be null");
        assertNotNull(event.getPayload(), "payload should not be null");
    }

    @Test
    void eventVersioningCustom() {
        // Given - event with custom version
        var event = new EventEnvelope(
            "appointment-created",
            "appointment",
            "apt-001",
            "scheduler-service",
            Map.of("reason", "new appointment")
        );

        // Set version explicitly
        event.setVersion(2);

        // When - version should be 2
        assertEquals(2, event.getVersion(),
            "event version should be settable to 2, got: " + event.getVersion());
    }

    @Test
    void eventVersioningSurvivesSerialization() throws Exception {
        // Given - event with version
        String version = "3";
        var event = new EventEnvelope(
            "appointment-created",
            "appointment",
            "apt-001",
            "scheduler-service",
            Map.of("reason", "new appointment")
        );
        event.setVersion(3);

        // When - serialize and deserialize
        String json = objectMapper.writeValueAsString(event);

        // Then - version should be in JSON
        assertTrue(json.contains("\"version\":3"),
            "version should survive JSON serialization: " + json);

        // And - can deserialize back
        EventEnvelope deserialized = objectMapper.readValue(json, EventEnvelope.class);
        assertEquals(3, deserialized.getVersion(),
            "version should survive JSON deserialization, got: " + deserialized.getVersion());
    }
}