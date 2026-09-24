package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "demand_forecasts")
@Data
public class DemandForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private InventoryItem item;

    private LocalDateTime forecastPeriodStart;

    private LocalDateTime forecastPeriodEnd;

    private Integer predictedDemand;

    private Double confidenceScore;

    private LocalDateTime predictedStockOutDate;

    private String forecastMethod; // STATISTICAL, AI_ENHANDED, SIMPLE_AVERAGE

    private String notes;

    private LocalDateTime createdAt;

    public DemandForecast() {
    }
}