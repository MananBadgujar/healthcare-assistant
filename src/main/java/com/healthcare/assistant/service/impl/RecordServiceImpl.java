package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Record;
import com.healthcare.assistant.repository.RecordRepository;
import com.healthcare.assistant.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordRepository repository;

    @Autowired
    public RecordServiceImpl(RecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public Record createSummary(Long patientId, String content) {
        Record record = new Record(patientId, "SUMMARY", content, "2025-01-01T00:00:00");
        return repository.save(record);
    }
}