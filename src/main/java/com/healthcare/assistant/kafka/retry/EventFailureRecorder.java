package com.healthcare.assistant.kafka.retry;

import com.healthcare.assistant.kafka.audit.EventAudit;
import com.healthcare.assistant.kafka.entity.EventFailure;
import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.repository.EventFailureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Persists retry/DLQ failure state for the existing {@link EventFailure} entity.
 * Wired into the Spring Kafka {@code DefaultErrorHandler} so retryable failures
 * record attempt counts and exhausted failures transition to SENT_TO_DLQ with a
 * {@code sent-to-dlq} audit entry.
 */
@Component
public class EventFailureRecorder {

    private static final Logger logger = LoggerFactory.getLogger(EventFailureRecorder.class);

    private final EventFailureRepository failureRepository;
    private final EventAudit eventAudit;

    public EventFailureRecorder(EventFailureRepository failureRepository, EventAudit eventAudit) {
        this.failureRepository = failureRepository;
        this.eventAudit = eventAudit;
    }

    @Transactional
    public void recordRetryAttempt(EventEnvelope event, String sourceTopic, int deliveryAttempt, Throwable ex) {
        String eventId = event.getEventId();
        Optional<EventFailure> existing = failureRepository.findByEventId(eventId);
        EventFailure failure = existing.orElseGet(EventFailure::new);
        failure.setEventId(eventId);
        failure.setEventType(event.getEventType());
        failure.setAggregateId(event.getAggregateId());
        failure.setCorrelationId(event.getCorrelationId());
        failure.setSourceTopic(sourceTopic);
        failure.setAttemptCount(Math.max(deliveryAttempt, failure.getAttemptCount() != null ? failure.getAttemptCount() : 0));
        failure.setFailureReason(ex != null ? truncate(ex.getMessage(), 2000) : "retryable failure");
        failure.setExceptionDetails(ex != null ? truncate(ex.toString(), 4000) : null);
        failure.setFailureTimestamp(Instant.now());
        if (failure.getDlqStatus() == null) {
            failure.setDlqStatus("PENDING");
        }
        failureRepository.save(failure);
        logger.debug("Recorded retry attempt {} for event {}", deliveryAttempt, eventId);
    }

    @Transactional
    public void recordSentToDlq(EventEnvelope event, String sourceTopic, int attemptCount, Throwable ex) {
        String eventId = event.getEventId();
        Optional<EventFailure> existing = failureRepository.findByEventId(eventId);
        EventFailure failure = existing.orElseGet(EventFailure::new);
        failure.setEventId(eventId);
        failure.setEventType(event.getEventType());
        failure.setAggregateId(event.getAggregateId());
        failure.setCorrelationId(event.getCorrelationId());
        failure.setSourceTopic(sourceTopic);
        failure.setAttemptCount(Math.max(attemptCount, failure.getAttemptCount() != null ? failure.getAttemptCount() : 0));
        failure.setFailureReason(ex != null ? truncate(ex.getMessage(), 2000) : "sent to DLQ");
        failure.setExceptionDetails(ex != null ? truncate(ex.toString(), 4000) : null);
        failure.setFailureTimestamp(Instant.now());
        failure.setDlqStatus("SENT_TO_DLQ");
        failureRepository.save(failure);
        eventAudit.record(event, "sent-to-dlq", sourceTopic);
        logger.info("Event {} marked SENT_TO_DLQ from topic {}", eventId, sourceTopic);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
