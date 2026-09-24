package com.healthcare.assistant.kafka.producer;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;

@Component
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.default-topic:healthcare.appointment.events.created}")
    private String defaultTopic;

    @Autowired
    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Autowired
    private ObjectMapper objectMapper;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void publish(EventEnvelope event, String topic) {
        String payload = serialize(event);
        kafkaTemplate.send(topic, event.getEventId(), payload);
    }

    public void publish(String eventType, String aggregateType, String aggregateId,
                        String source, Object payload, String topic) {
        EventEnvelope event = new EventEnvelope(eventType, aggregateType, aggregateId, source, payload);
        publish(event, topic);
    }

    public void publish(EventEnvelope event) {
        publish(event, defaultTopic);
    }

    public String serialize(EventEnvelope event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            logger.error("Failed to serialize event: {}", e.getMessage());
            return event.toString();
        }
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }
}