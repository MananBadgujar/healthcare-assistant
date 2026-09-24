package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Long> {
    Optional<Record> findByPatientId(Long patientId);
}