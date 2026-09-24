package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.TriageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TriageRecordRepository extends JpaRepository<TriageRecord, Long> {
}