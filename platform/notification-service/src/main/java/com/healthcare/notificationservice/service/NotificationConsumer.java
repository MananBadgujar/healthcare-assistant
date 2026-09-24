package com.healthcare.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.notificationservice.entity.Notification;
import com.healthcare.notificationservice.repo.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationRepository repo;
    private final ObjectMapper om;

    public NotificationConsumer(NotificationRepository repo, ObjectMapper om) {
        this.repo = repo; this.om = om;
    }

    @KafkaListener(topics = {
            "healthcare.appointment.events.created",
            "healthcare.appointment.events.cancelled",
            "healthcare.medication.events.refill-requested",
            "healthcare.billing.events.payment-completed",
            "healthcare.encounter.events.clinical-alert",
            "healthcare.ai.events.triage-completed"
    }, groupId = "notification-service-group")
    @Transactional
    public void onEvent(String raw) {
        try {
            DomainEvent e = om.readValue(raw, DomainEvent.class);
            if (e.getEventId() == null) { log.warn("notification: dropping event without id"); return; }
            // Idempotent consumption: duplicates are acknowledged without side effects.
            if (repo.existsByEventId(e.getEventId())) {
                log.info("notification: duplicate {} ignored", e.getEventId());
                return;
            }
            Notification n = new Notification();
            n.setEventId(e.getEventId());
            n.setType(e.getEventName());
            n.setRecipient(String.valueOf(e.getPayload() == null ? "" : e.getPayload().getOrDefault("recipient", "")));
            n.setMessage("[" + e.getEventName() + "] entity " + e.getEntityType() + ":" + e.getEntityId());
            repo.save(n);
            log.info("notification: stored {} corr={}", e.getEventName(), e.getCorrelationId());
        } catch (Exception ex) {
            // Invalid events are logged and skipped so they never break the domain transaction.
            log.warn("notification: invalid event skipped: {}", ex.toString());
        }
    }
}
