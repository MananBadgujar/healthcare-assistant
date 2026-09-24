package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/scheduling")
public class SchedulingBulkRescheduleController {

    @PostMapping("/bulk-reschedule")
    public ResponseEntity<Void> bulkReschedule(@RequestBody java.util.Map<String, String> request) {
        // TODO: implement bulk rescheduling logic
        return ResponseEntity.noContent().build();
    }
}