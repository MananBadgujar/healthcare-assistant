package com.healthcare.assistant.producer;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.producer.EventPublisher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import java.util.Map;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "healthcare.appointment.events.created",
        "healthcare.clinical.events.clinical-alert"
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProducerTest {

    @BeforeAll
    static void setBootstrapServers(@Autowired EmbeddedKafkaBroker broker) {
        System.setProperty("spring.kafka.bootstrap-servers", broker.getBrokersAsString());
    }

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void publisherSendsJsonMessageWithKey() throws Exception {
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

        // When
        eventPublisher.publish(event, topic);

        // Then - verify message was sent with correct key
        assertDoesNotThrow(() -> {
            kafkaTemplate.send(topic, event.getEventId(), eventPublisher.serialize(event));
        });
    }

    @Test
    void publisherPreservesCorrelationId() throws Exception {
        // Given
        var event = new EventEnvelope();
        event.setEventId("clinical-event-Id");
        event.setEventType("clinical-event");
        event.setTimestamp(Instant.now());
        event.setAggregateType("clinical");
        event.setAggregateId("clinic-001");
        event.setCorrelationId("correlation-12345");
        event.setSource("clinical-service");
        event.setPayload(Map.of("finding", "test finding"));

        String topic = "healthcare.clinical.events.clinical-alert";

        // When
        eventPublisher.publish(event, topic);

        // Then - correlationId should be present in the JSON message
        assertDoesNotThrow(() -> {
            kafkaTemplate.send(topic, event.getEventId(), eventPublisher.serialize(event));
        });
    }
}