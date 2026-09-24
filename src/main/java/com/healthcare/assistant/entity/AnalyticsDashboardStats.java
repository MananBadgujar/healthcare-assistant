package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_dashboard_stats")
@Data
public class AnalyticsDashboardStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String statType; // PATIENT_COUNT, APPOINTMENT_COUNT, ACTIVE_CONDITIONS, HIGH_RISK_PATIENTS, MEDICATION_ADHERENCE, INVENTORY_ALERTS, TELEHEALTH_SESSIONS

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private Integer countValue;

    private String filterCriteria; // JSON string for date/facility/region filtering

    private String unit; // e.g., "count", "percentage", "value"

    private LocalDateTime calculatedAt;

    private LocalDateTime updatedAt;

    public AnalyticsDashboardStats() {
    }

    public AnalyticsDashboardStats(String statType, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this.statType = statType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.calculatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}