package com.healthcare.assistant.kafka.repository;

import com.healthcare.assistant.kafka.entity.ProcessedEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    Optional<ProcessedEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    long countByEventId(String eventId);

    boolean existsByEventIdAndConsumerId(String eventId, String consumerId);

    Optional<ProcessedEvent> findByEventIdAndConsumerId(String eventId, String consumerId);
}