package com.healthcare.assistant.kafka.audit;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.entity.EventAuditLog;
import com.healthcare.assistant.kafka.repository.EventAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class EventAudit {

    private static final Logger logger = LoggerFactory.getLogger(EventAudit.class);

    private final EventAuditLogRepository auditLogRepository;

    public EventAudit(EventAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(EventEnvelope event, String status) {
        record(event, status, null);
    }

    @Transactional
    public void record(EventEnvelope event, String status, String details) {
        String correlationId = event.getCorrelationId();
        String eventId = event.getEventId();
        String eventType = event.getEventType();
        Instant timestamp = Instant.now();

        String auditEntry = String.format(
                "AUDIT: eventId=%s, eventType=%s, correlationId=%s, status=%s, timestamp=%s, details=%s",
                eventId, eventType, correlationId, status, timestamp, details);

        logger.info(auditEntry);

        // Persist audit log to database
        // Source is part of the event contract (EventEnvelope.source = producing service).
        // If a producer omitted it, derive a meaningful value from the aggregate type
        // rather than persisting NULL against the NOT NULL column.
        String source = event.getSource();
        if (source == null || source.isBlank()) {
            source = event.getAggregateType() != null && !event.getAggregateType().isBlank()
                    ? event.getAggregateType() + "-service"
                    : "unknown-service";
            logger.warn("Event {} missing source; derived audit source '{}' from aggregateType='{}'",
                    eventId, source, event.getAggregateType());
        }
        EventAuditLog logEntry = new EventAuditLog();
        logEntry.setEventId(eventId);
        logEntry.setEventType(eventType);
        logEntry.setCorrelationId(correlationId);
        logEntry.setSource(source);
        logEntry.setStatus(status);
        logEntry.setTimestamp(timestamp);
        logEntry.setDetails(details);
        auditLogRepository.save(logEntry);
    }

    @Transactional
    public void record(String auditKey, String status) {
        logger.info("AUDIT: {} - {}", auditKey, status);
    }

    @Transactional
    public String generateAuditKey(String eventId, String consumerId) {
        return String.format("event-%s-consumer-%s", eventId, consumerId);
    }
}