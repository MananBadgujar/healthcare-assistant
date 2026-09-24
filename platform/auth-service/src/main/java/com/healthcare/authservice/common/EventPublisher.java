package com.healthcare.authservice.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/** Reliable-enough dev publisher: DB commit happens first, then publish; consumer is idempotent. */
@Component
public class EventPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om;

    public EventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper om) {
        this.kafka = kafka;
        this.om = om;
    }

    @PostConstruct
    public void init() {
        System.out.println("[EventPublisher] Kafka publisher initialized");
    }

    public void publish(String topic, DomainEvent event) {
        try {
            if (event.getCorrelationId() == null) event.setCorrelationId(MDC.get("correlationId"));
            kafka.send(topic, event.getEntityId(), om.writeValueAsString(event));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish event " + event.getEventName(), e);
        }
    }
}
