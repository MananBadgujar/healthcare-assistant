package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "population_health_reports")
@Data
public class PopulationHealthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportType; // DEMOGRAPHICS, DISEASE_PREVALENCE, CHRONIC_CONDITIONS, MEDICATION_ADHERENCE, UTILIZATION

    private LocalDateTime reportingPeriodStart;

    private LocalDateTime reportingPeriodEnd;

    private Integer totalPatients;

    private Integer activePatients;

    private Double averageAge;

    private String genderDistribution; // JSON string: {"male": 120, "female": 115}

    private Integer newCasesThisPeriod;

    private Integer resolvedCasesThisPeriod;

    private LocalDateTime generatedAt;

    private LocalDateTime updatedAt;

    public PopulationHealthReport() {
    }

    public PopulationHealthReport(String reportType, LocalDateTime reportingPeriodStart, LocalDateTime reportingPeriodEnd) {
        this.reportType = reportType;
        this.reportingPeriodStart = reportingPeriodStart;
        this.reportingPeriodEnd = reportingPeriodEnd;
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}