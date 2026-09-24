package com.healthcare.assistant.service;

import java.util.Optional;

import com.healthcare.assistant.entity.Record;

public interface RecordService {
    Record createSummary(Long patientId, String content);
}