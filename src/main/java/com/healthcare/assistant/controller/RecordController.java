package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Record;
import com.healthcare.assistant.dto.SummaryRequest;
import com.healthcare.assistant.service.RecordService;
import com.healthcare.assistant.service.PatientContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.assistant.entity.Record;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {

    private final RecordService recordService;

    @Autowired
    private PatientContextService patientContextService;

    public RecordController(RecordService recordService, PatientContextService patientContextService) {
        this.recordService = recordService;
        this.patientContextService = patientContextService;
    }

    @PostMapping("/summary")
    public ResponseEntity<Record> createSummary(@RequestBody SummaryRequest request) {
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!request.getPatientId().equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Record record = recordService.createSummary(request.getPatientId(), request.getContent());
        return ResponseEntity.ok(record);
    }
}