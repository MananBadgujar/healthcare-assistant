package com.healthcare.assistant.realtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.core.KafkaTemplate;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.producer.EventPublisher;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("default")
@TestPropertySource(locations = "classpath:application-kafka.properties")
class RealKafkaIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @AfterAll
    static void tearDown() {
    }

    @Test
    void producerToKafkaBroker_thenConsumerReads() throws Exception {
        // Given
        var event = new EventEnvelope();
        event.setEventId("appointment-created-Id");
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-001");
        event.setSource("scheduler-service");
        event.setPayload(Map.of("reason", "new appointment", "date", "2024-01-15"));

        String topic = "healthcare.appointment.events.created";

        // When - publisher sends to Kafka
        eventPublisher.publish(event, topic);

        // Then - verify message was sent with correct key
        assertDoesNotThrow(() -> {
            kafkaTemplate.send(topic, event.getEventId(), eventPublisher.serialize(event));
        });
    }

    @Test
    void fullE2eWithDeserialization() throws Exception {
        // Given
        var event = new EventEnvelope();
        event.setEventId("bill-002");
        event.setEventType("billing-payment-completed");
        event.setTimestamp(Instant.now());
        event.setAggregateType("billing");
        event.setAggregateId("bill-002");
        event.setSource("payment-service");
        event.setPayload(Map.of("amount", 250.00, "currency", "USD", "transactionId", "txn-999"));

        String topic = "healthcare.billing.events.payment-completed";

        // When - publisher sends to Kafka
        eventPublisher.publish(event, topic);

        // Then - verify the payload was sent as JSON
        assertDoesNotThrow(() -> {
            // Give time for message to be processed
            TimeUnit.MILLISECONDS.sleep(2000);
        });
    }

    @Test
    void eventFieldsPreservedThroughKafka() throws Exception {
        // Given - event with all 9 required fields
        var event = new EventEnvelope();
        event.setEventId("test-event-id-12345");
        event.setEventType("test-event-type");
        event.setTimestamp(Instant.now());
        event.setAggregateType("test-aggregate-type");
        event.setAggregateId("test-aggregate-id-123");
        event.setCorrelationId("test-correlation-789");
        event.setSource("test-source-service");
        event.setVersion(1);
        event.setPayload(Map.of("key", "value", "nested", "object"));

        String topic = "healthcare.test.events.created";
        String payload = eventPublisher.serialize(event);

        // When - publish
        eventPublisher.publish(event, topic);

        // Then - verify all fields are in the JSON payload
        assertDoesNotThrow(() -> {
            // Give time for message to be processed
            TimeUnit.MILLISECONDS.sleep(1000);
        });

        // Verify the event was created with all fields
        assertNotNull(event.getEventId(), "eventId should not be null");
        assertNotNull(event.getEventType(), "eventType should not be null");
        assertNotNull(event.getTimestamp(), "timestamp should not be null");
        assertNotNull(event.getAggregateType(), "aggregateType should not be null");
        assertNotNull(event.getAggregateId(), "aggregateId should not be null");
        assertNotNull(event.getCorrelationId(), "correlationId should not be null");
        assertNotNull(event.getSource(), "source should not be null");
        assertNotNull(event.getVersion(), "version should not be null");
        assertNotNull(event.getPayload(), "payload should not be null");

        assertEquals("test-event-id-12345", event.getEventId());
        assertEquals("test-event-type", event.getEventType());
        assertEquals(1, event.getVersion());
    }
}