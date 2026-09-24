package com.healthcare.assistant.kafka.idempotency;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.audit.EventAudit;
import com.healthcare.assistant.kafka.entity.ProcessedEvent;
import com.healthcare.assistant.kafka.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class IdempotencyManager {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyManager.class);

    private final ProcessedEventRepository processedEventRepository;
    private final EventAudit eventAudit;

    public IdempotencyManager(ProcessedEventRepository processedEventRepository,
                              EventAudit eventAudit) {
        this.processedEventRepository = processedEventRepository;
        this.eventAudit = eventAudit;
    }

    @Transactional
    public boolean isProcessed(String eventId, String consumerId) {
        Optional<ProcessedEvent> existing = processedEventRepository.findByEventIdAndConsumerId(eventId, consumerId);
        if (existing.isPresent()) {
            logger.debug("Event already processed (idempotent): eventId={}, consumerId={}", eventId, consumerId);
            eventAudit.record(eventAudit.generateAuditKey(eventId, consumerId), "already-processed");
            return true;
        }
        return false;
    }

    @Transactional
    public void markProcessed(String eventId, String consumerId) {
        markProcessed(eventId, null, consumerId);
    }

    @Transactional
    public void markProcessed(String eventId, String aggregateId, String consumerId) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setAggregateId(aggregateId != null ? aggregateId : eventId);
        processedEvent.setConsumerId(consumerId);
        processedEvent.setStatus("PROCESSED");
        processedEvent.setProcessedAt(java.time.Instant.now());
        processedEventRepository.save(processedEvent);
        logger.info("Marked event as processed: eventId={}, consumerId={}", eventId, consumerId);
        eventAudit.record(eventAudit.generateAuditKey(eventId, consumerId), "marked-processed");
    }
}