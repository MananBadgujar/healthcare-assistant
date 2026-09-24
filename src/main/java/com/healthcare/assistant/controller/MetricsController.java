package com.healthcare.assistant.controller;

import java.util.Collections;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    @GetMapping
    public ResponseEntity<String> getMetrics() {
        return ResponseEntity.ok("{}");
    }
}