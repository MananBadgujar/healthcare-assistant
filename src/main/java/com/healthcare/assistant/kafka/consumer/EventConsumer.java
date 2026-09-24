package com.healthcare.assistant.kafka.consumer;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.audit.EventAudit;
import com.healthcare.assistant.kafka.idempotency.IdempotencyManager;
import com.healthcare.assistant.kafka.exception.RetryableException;
import com.healthcare.assistant.kafka.exception.NonRetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private final IdempotencyManager idempotencyManager;
    private final EventAudit eventAudit;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventConsumer(IdempotencyManager idempotencyManager,
                         EventAudit eventAudit,
                         KafkaTemplate<String, byte[]> kafkaTemplate,
                         ObjectMapper objectMapper) {
        this.idempotencyManager = idempotencyManager;
        this.eventAudit = eventAudit;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void consume(EventEnvelope event, String consumerId) {
        logger.info("Processing event: {} for aggregate {} by consumer {}", event.getEventType(), event.getAggregateId(), consumerId);

        // Check idempotency - skip if already processed
        if (!idempotencyManager.isProcessed(event.getEventId(), consumerId)) {
            // Record audit before processing
            eventAudit.record(event, "processing-started");

            try {
                process(event);
                // Successful processing - return normally, DefaultErrorHandler will acknowledge
                idempotencyManager.markProcessed(event.getEventId(), event.getAggregateId(), consumerId);
                eventAudit.record(event, "processing-completed");
                logger.info("Event processed successfully: {} by consumer {}", event.getEventId(), consumerId);
            } catch (RetryableException e) {
                // Retryable failure - throw, let Spring Kafka retry infrastructure handle it
                logger.warn("Retryable failure for event {}, will retry", event.getEventId());
                eventAudit.record(event, "retry-attempted");
                throw e;
            } catch (NonRetryableException e) {
                // Non-retryable failure - let DefaultErrorHandler handle DLQ via DeadLetterPublishingRecoverer
                logger.error("Non-retryable failure for event {}: {}", event.getEventId(), e.getMessage());
                eventAudit.record(event, "sent-to-dlq", "non-retryable-failure");
                throw e;
            } catch (Exception e) {
                // Unknown failure - treat as retryable to trigger retry/DLQ flow
                logger.error("Unexpected error processing event {}: {}", event.getEventId(), e.getMessage());
                eventAudit.record(event, "processing-failed", e.getMessage());
                throw new RetryableException("Unexpected error: " + e.getMessage(), e);
            }
        } else {
            logger.debug("Event already processed (idempotent): {} by consumer {}", event.getEventId(), consumerId);
            // Return normally - DefaultErrorHandler will acknowledge
        }
    }

    protected void process(EventEnvelope event) {
        String eventType = event.getEventType();
        String aggregateType = event.getAggregateType();
        String aggregateId = event.getAggregateId();

        // Record audit with event details
        eventAudit.record(event, "processing");

        switch (eventType) {
            case "appointment-created":
                logger.info("Creating appointment aggregate {} for appointment-created event", aggregateId);
                eventAudit.record(event, "appointment-created", "aggregateId=" + aggregateId);
                break;
            case "appointment-confirmed":
                logger.info("Confirming appointment aggregate {} for appointment-confirmed event", aggregateId);
                eventAudit.record(event, "appointment-confirmed", "aggregateId=" + aggregateId);
                break;
            case "appointment-cancelled":
                logger.info("Cancelling appointment aggregate {} for appointment-cancelled event", aggregateId);
                eventAudit.record(event, "appointment-cancelled", "aggregateId=" + aggregateId);
                break;
            case "billing-payment-completed":
                logger.info("Processing billing payment for aggregate {}: completed", aggregateId);
                eventAudit.record(event, "billing-payment-completed", "aggregateId=" + aggregateId);
                break;
            case "billing-claim-submitted":
                logger.info("Submitting billing claim for aggregate {}: billing-claim-submitted", aggregateId);
                eventAudit.record(event, "billing-claim-submitted", "aggregateId=" + aggregateId);
                break;
            case "inventory-low-stock":
                logger.info("Triggering reorder for inventory aggregate {}: low-stock", aggregateId);
                eventAudit.record(event, "inventory-low-stock", "aggregateId=" + aggregateId);
                break;
            case "clinical-event":
                logger.info("Processing clinical event for aggregate {}: clinical-event", aggregateId);
                eventAudit.record(event, "clinical-event", "aggregateId=" + aggregateId);
                break;
            case "ai-event":
                logger.info("Processing AI event for aggregate {}: ai-event", aggregateId);
                eventAudit.record(event, "ai-event", "aggregateId=" + aggregateId);
                break;
            case "notification-event":
                logger.info("Sending notification for aggregate {}: notification-event", aggregateId);
                eventAudit.record(event, "notification-event", "aggregateId=" + aggregateId);
                break;
            case "appointment-created-fail":
                // Test event type that always throws RetryableException for retry testing
                logger.warn("Test failure event received, throwing RetryableException for event {}", event.getEventId());
                throw new RetryableException("Test failure for retry: " + event.getEventId(), new RuntimeException("Test retry cause"));
            default:
                logger.warn("Unknown event type {} received for aggregate {}", eventType, aggregateId);
                break;
        }
    }

    private byte[] serialize(EventEnvelope event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            logger.error("Failed to serialize event {}: {}", event.getEventId(), e.getMessage());
            return event.toString().getBytes();
        }
    }
}