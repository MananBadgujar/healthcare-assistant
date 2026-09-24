package com.healthcare.assistant.kafka.consumer;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.audit.EventAudit;
import com.healthcare.assistant.kafka.idempotency.IdempotencyManager;
import com.healthcare.assistant.kafka.exception.RetryableException;
import com.healthcare.assistant.kafka.exception.NonRetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

@Component
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private final EventConsumer eventConsumer;
    private final ObjectMapper objectMapper;

    public KafkaConsumerConfig(EventConsumer eventConsumer, ObjectMapper objectMapper) {
        this.eventConsumer = eventConsumer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"${kafka.topics.patient.created}",
                    "${kafka.topics.patient.updated}"},
            groupId = "${kafka.consumer.group.id.patient:patient-consumer-group}")
    public void patientEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "patient-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.appointment.created}",
                    "${kafka.topics.appointment.updated}",
                    "${kafka.topics.appointment.cancelled}",
                    "${kafka.topics.appointment.confirmed}",
                    "${kafka.topics.appointment.completed}",
                    "${kafka.topics.appointment.no-show}",
                    "${kafka.topics.appointment.status-changed}"},
            groupId = "${kafka.consumer.group.id.appointment:appointment-consumer-group}")
    public void appointmentEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "appointment-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.encounter.completed}",
                    "${kafka.topics.encounter.lab-result}",
                    "${kafka.topics.encounter.clinical-alert}"},
            groupId = "${kafka.consumer.group.id.encounter:encounter-consumer-group}")
    public void encounterEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "encounter-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.medication.created}",
                    "${kafka.topics.medication.refill-requested}"},
            groupId = "${kafka.consumer.group.id.medication:medication-consumer-group}")
    public void medicationEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "medication-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.billing.payment-completed}",
                    "${kafka.topics.billing.claim-submitted}",
                    "${kafka.topics.billing.claim-approved}",
                    "${kafka.topics.billing.claim-rejected}"},
            groupId = "${kafka.consumer.group.id.billing:billing-consumer-group}")
    public void billingEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "billing-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.claim.events}",
                    "${kafka.topics.claim.submitted}",
                    "${kafka.topics.claim.approved}",
                    "${kafka.topics.claim.rejected}"},
            groupId = "${kafka.consumer.group.id.claim:claim-consumer-group}")
    public void claimEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "claim-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.ai.lab-interpretation}",
                    "${kafka.topics.ai.clinical-alert}",
                    "${kafka.topics.ai.triage-completed}"},
            groupId = "${kafka.consumer.group.id.ai:ai-consumer-group}")
    public void aiEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "ai-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.notification.appointment-reminder}",
                    "${kafka.topics.notification.appointment-cancellation}",
                    "${kafka.topics.notification.payment-confirmation}",
                    "${kafka.topics.notification.lab-result-available}"},
            groupId = "${kafka.consumer.group.id.notification:notification-consumer-group}")
    public void notificationEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "notification-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.inventory.low-stock}",
                    "${kafka.topics.inventory.near-expiry}",
                    "${kafka.topics.inventory.stock-received}",
                    "${kafka.topics.inventory.stock-consumed}"},
            groupId = "${kafka.consumer.group.id.inventory:inventory-consumer-group}")
    public void inventoryEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "inventory-consumer-group");
    }

    @KafkaListener(
            topics = {"${kafka.topics.telehealth.session-completed}",
                    "${kafka.topics.telehealth.session-reminder}"},
            groupId = "${kafka.consumer.group.id.telehealth:telehealth-consumer-group}")
    public void telehealthEventListener(String payload) {
        EventEnvelope event = deserialize(payload);
        eventConsumer.consume(event, "telehealth-consumer-group");
    }

    private EventEnvelope deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, EventEnvelope.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize Kafka payload: {}", e.getMessage());
            throw new RuntimeException("Deserialization failed for payload: " + (payload != null ? payload.substring(0, Math.min(payload.length(), 100)) : "null"), e);
        }
    }
}