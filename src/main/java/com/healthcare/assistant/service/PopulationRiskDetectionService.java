package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.PopulationRiskSegment;
import java.time.LocalDateTime;
import java.util.List;

public interface PopulationRiskDetectionService {
    List<PopulationRiskSegment> detectRisksByReport(PopulationHealthReport report);
    List<PopulationRiskSegment> detectEmergingRisks(LocalDateTime fromDate);
    List<PopulationRiskSegment> detectByRiskCategory(String riskCategory);
    PopulationRiskSegment assessPatientRisk(Long patientId, LocalDateTime assessmentDate);
}