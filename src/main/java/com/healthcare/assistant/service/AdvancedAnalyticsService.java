package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.entity.DemandForecast;
import java.util.List;

public interface AdvancedAnalyticsService {
    String explainTrend(DemandForecast forecast);
    String detectAnomaly(InventoryItem item);
    String forecastInterpretation(DemandForecast forecast);
    String operationalRecommendation(InventoryItem item, String forecastMethod);
    Double confidenceScoreInterpretation(Double confidence);
    boolean validateForecastOutput(DemandForecast forecast);
}