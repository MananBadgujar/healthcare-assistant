package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.AnalyticsDashboardStats;
import com.healthcare.assistant.service.AnalyticsDashboardService;
import java.time.LocalDateTime;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/dashboard")
public class AnalyticsDashboardController {

    private final AnalyticsDashboardService analyticsDashboardService;

    @Autowired
    public AnalyticsDashboardController(AnalyticsDashboardService analyticsDashboardService) {
        this.analyticsDashboardService = analyticsDashboardService;
    }

    @PostMapping("/stats")
    public ResponseEntity<AnalyticsDashboardStats> createStats(@Valid @RequestBody AnalyticsDashboardStats statsDto) {
        AnalyticsDashboardStats created = analyticsDashboardService.createStats(statsDto.getStatType(), statsDto.getPeriodStart(), statsDto.getPeriodEnd());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/stats/{statType}")
    public ResponseEntity<List<AnalyticsDashboardStats>> getStatsByType(@PathVariable String statType) {
        List<AnalyticsDashboardStats> stats = analyticsDashboardService.getStatsByType(statType);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/period")
    public ResponseEntity<List<AnalyticsDashboardStats>> getStatsByPeriod(@RequestParam(name = "start") LocalDateTime start, @RequestParam(name = "end") LocalDateTime end) {
        List<AnalyticsDashboardStats> stats = analyticsDashboardService.getStatsByPeriod(start, end);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/overview")
    public ResponseEntity<List<AnalyticsDashboardStats>> getOverview() {
        List<AnalyticsDashboardStats> overview = analyticsDashboardService.getAllStats();
        return ResponseEntity.ok(overview);
    }
}