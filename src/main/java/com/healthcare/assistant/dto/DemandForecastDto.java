package com.healthcare.assistant.dto;

import java.time.LocalDateTime;

public class DemandForecastDto {
    private Long id;
    private Long itemId;
    private String itemSku;
    private String itemName;
    private LocalDateTime forecastPeriodStart;
    private LocalDateTime forecastPeriodEnd;
    private Integer predictedDemand;
    private Double confidenceScore;
    private LocalDateTime predictedStockOutDate;
    private String forecastMethod; // STATISTICAL, AI_ENHANCED, SIMPLE_AVERAGE
    private String notes;
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemSku() { return itemSku; }
    public void setItemSku(String itemSku) { this.itemSku = itemSku; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public LocalDateTime getForecastPeriodStart() { return forecastPeriodStart; }
    public void setForecastPeriodStart(LocalDateTime forecastPeriodStart) { this.forecastPeriodStart = forecastPeriodStart; }
    public LocalDateTime getForecastPeriodEnd() { return forecastPeriodEnd; }
    public void setForecastPeriodEnd(LocalDateTime forecastPeriodEnd) { this.forecastPeriodEnd = forecastPeriodEnd; }
    public Integer getPredictedDemand() { return predictedDemand; }
    public void setPredictedDemand(Integer predictedDemand) { this.predictedDemand = predictedDemand; }
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    public LocalDateTime getPredictedStockOutDate() { return predictedStockOutDate; }
    public void setPredictedStockOutDate(LocalDateTime predictedStockOutDate) { this.predictedStockOutDate = predictedStockOutDate; }
    public String getForecastMethod() { return forecastMethod; }
    public void setForecastMethod(String forecastMethod) { this.forecastMethod = forecastMethod; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}