package com.healthcare.assistant.kafka.repository;

import com.healthcare.assistant.kafka.entity.EventFailure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventFailureRepository extends JpaRepository<EventFailure, Long> {

    Optional<EventFailure> findByEventId(String eventId);

    List<EventFailure> findBySourceTopic(String sourceTopic);

    boolean existsByEventId(String eventId);
}