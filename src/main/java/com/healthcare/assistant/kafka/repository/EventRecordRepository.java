package com.healthcare.assistant.kafka.repository;

import com.healthcare.assistant.kafka.entity.EventRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRecordRepository extends JpaRepository<EventRecord, Long> {

    Optional<EventRecord> findByEventId(String eventId);

    List<EventRecord> findByAggregateId(String aggregateId);
}