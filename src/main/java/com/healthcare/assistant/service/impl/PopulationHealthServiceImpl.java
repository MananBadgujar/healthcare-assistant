package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.PopulationRiskSegment;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PopulationHealthReportRepository;
import com.healthcare.assistant.repository.PopulationRiskSegmentRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.PopulationHealthService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PopulationHealthServiceImpl implements PopulationHealthService {

    @Autowired
    private PopulationHealthReportRepository reportRepository;

    @Autowired
    private PopulationRiskSegmentRepository riskSegmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public PopulationHealthReport createReport(String reportType, LocalDateTime start, LocalDateTime end) {
        PopulationHealthReport report = new PopulationHealthReport(reportType, start, end);
        return reportRepository.save(report);
    }

    @Override
    public PopulationHealthReport getReportById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }

    @Override
    public List<PopulationHealthReport> getReportsByType(String reportType) {
        return reportRepository.findByReportTypeOrderByGeneratedAtDesc(reportType);
    }

    @Override
    public List<PopulationHealthReport> getReportsByPeriod(LocalDateTime start, LocalDateTime end) {
        return reportRepository.findByGeneratedAtBetween(start, end);
    }

    @Override
    @Transactional
    public List<PopulationRiskSegment> detectRisks(PopulationHealthReport report) {
        List<PopulationRiskSegment> segments = riskSegmentRepository.findByReportId(report.getId());
        if (segments.isEmpty()) {
            segments = generateRiskSegments(report);
        }
        return segments;
    }

    @Override
    @Transactional
    public PopulationRiskSegment createRiskSegment(String riskCategory, String description, String indicators, Integer patientCount, Double riskScore) {
        PopulationRiskSegment segment = new PopulationRiskSegment(riskCategory, description, indicators, patientCount, riskScore);
        return riskSegmentRepository.save(segment);
    }

    private List<PopulationRiskSegment> generateRiskSegments(PopulationHealthReport report) {
        List<PopulationRiskSegment> segments = java.util.Collections.emptyList();

        if ("DEMOGRAPHICS".equals(report.getReportType())) {
            segments = generateDemographicRisks(report);
        } else if ("DISEASE_PREVALENCE".equals(report.getReportType())) {
            segments = generateDiseasePrevalenceRisks(report);
        } else if ("CHRONIC_CONDITIONS".equals(report.getReportType())) {
            segments = generateChronicConditionRisks(report);
        } else if ("MEDICATION_ADHERENCE".equals(report.getReportType())) {
            segments = generateMedicationAdherenceRisks(report);
        } else if ("UTILIZATION".equals(report.getReportType())) {
            segments = generateUtilizationRisks(report);
        }

        if (segments != null && !segments.isEmpty()) {
            segments.forEach(segment -> riskSegmentRepository.save(segment));
        }
        return segments;
    }

    private List<PopulationRiskSegment> generateDemographicRisks(PopulationHealthReport report) {
        Integer totalPatients = report.getTotalPatients();
        Double averageAge = report.getAverageAge();

        java.util.List<PopulationRiskSegment> segments = java.util.Collections.emptyList();

        if (averageAge != null && averageAge > 65.0) {
            segments = java.util.List.of(
                    new PopulationRiskSegment("HIGH_RISK", "Aging population with average age > 65",
                            "avg_age:" + averageAge, totalPatients, 0.8));
        }

        if (totalPatients != null && totalPatients > 1000) {
            java.util.List<PopulationRiskSegment> additional = java.util.List.of(
                    new PopulationRiskSegment("MODERATE_RISK", "Large patient population requires monitoring",
                            "patient_count:" + totalPatients, totalPatients, 0.5));
            segments = java.util.stream.Stream.concat(segments.stream(), additional.stream())
                    .collect(java.util.stream.Collectors.toList());
        }

        return segments;
    }

    private Long totalPatientCount() {
        try {
            return entityManager.createQuery("SELECT COUNT(p) FROM Patient p", Long.class).getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }

    private List<PopulationRiskSegment> generateDiseasePrevalenceRisks(PopulationHealthReport report) {
        String description = report.getReportType() + " analysis for period " +
                report.getReportingPeriodStart() + " to " + report.getReportingPeriodEnd();

        return java.util.List.of(
                new PopulationRiskSegment("HIGH_RISK", "Disease prevalence detected requiring attention",
                        "report_type:" + report.getReportType(), report.getTotalPatients(), 0.75));
    }

    private List<PopulationRiskSegment> generateChronicConditionRisks(PopulationHealthReport report) {
        return java.util.List.of(
                new PopulationRiskSegment("MODERATE_RISK", "Chronic condition trends monitoring needed",
                        "report_type:" + report.getReportType(), report.getTotalPatients(), 0.55));
    }

    private List<PopulationRiskSegment> generateMedicationAdherenceRisks(PopulationHealthReport report) {
        return java.util.List.of(
                new PopulationRiskSegment("HIGH_RISK", "Medication adherence below threshold",
                        "report_type:" + report.getReportType(), report.getTotalPatients(), 0.8));
    }

    private List<PopulationRiskSegment> generateUtilizationRisks(PopulationHealthReport report) {
        return java.util.List.of(
                new PopulationRiskSegment("MODERATE_RISK", "Appointment utilization patterns require review",
                        "report_type:" + report.getReportType(), report.getTotalPatients(), 0.5));
    }
}