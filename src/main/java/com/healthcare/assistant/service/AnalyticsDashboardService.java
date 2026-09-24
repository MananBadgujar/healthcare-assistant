package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.AnalyticsDashboardStats;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsDashboardService {
    AnalyticsDashboardStats createStats(String statType, LocalDateTime start, LocalDateTime end);
    AnalyticsDashboardStats getStatsById(Long id);
    List<AnalyticsDashboardStats> getStatsByType(String statType);
    List<AnalyticsDashboardStats> getStatsByPeriod(LocalDateTime start, LocalDateTime end);
    List<AnalyticsDashboardStats> getAllStats();
}