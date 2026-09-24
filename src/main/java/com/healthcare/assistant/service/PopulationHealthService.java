package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.PopulationRiskSegment;
import java.time.LocalDateTime;
import java.util.List;

public interface PopulationHealthService {
    PopulationHealthReport createReport(String reportType, LocalDateTime start, LocalDateTime end);
    PopulationHealthReport getReportById(Long id);
    List<PopulationHealthReport> getReportsByType(String reportType);
    List<PopulationHealthReport> getReportsByPeriod(LocalDateTime start, LocalDateTime end);
    List<PopulationRiskSegment> detectRisks(PopulationHealthReport report);
    PopulationRiskSegment createRiskSegment(String riskCategory, String description, String indicators, Integer patientCount, Double riskScore);
}