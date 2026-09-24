package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "population_risk_segments")
@Data
public class PopulationRiskSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String riskCategory; // HIGH_RISK, MODERATE_RISK, LOW_RISK, EMERGING

    private String description;

    private String indicators; // JSON string of risk indicators

    private Integer patientCount;

    private Double riskScore; // 0.0 to 1.0

    private LocalDateTime detectedAt;

    private LocalDateTime lastUpdated;

    private String triggers; // What triggered this risk detection

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private PopulationHealthReport report;

    private String affectedPopulationFilter; // JSON filter criteria

    public PopulationRiskSegment() {
    }

    public PopulationRiskSegment(String riskCategory, String description, String indicators, Integer patientCount, Double riskScore) {
        this.riskCategory = riskCategory;
        this.description = description;
        this.indicators = indicators;
        this.patientCount = patientCount;
        this.riskScore = riskScore;
        this.detectedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.isActive = true;
    }
}