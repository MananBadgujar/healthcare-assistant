package com.healthcare.assistant.kafka.repository;

import com.healthcare.assistant.kafka.entity.EventAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventAuditLogRepository extends JpaRepository<EventAuditLog, Long> {

    java.util.List<EventAuditLog> findByEventId(String eventId);

    java.util.List<EventAuditLog> findByEventIdAndEventType(String eventId, String eventType);

    java.util.List<EventAuditLog> findByCorrelationId(String correlationId);
}