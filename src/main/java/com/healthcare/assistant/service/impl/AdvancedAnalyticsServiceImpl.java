package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.service.AdvancedAnalyticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdvancedAnalyticsServiceImpl implements AdvancedAnalyticsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String explainTrend(DemandForecast forecast) {
        if (forecast == null) {
            return "No forecast data available for trend explanation.";
        }

        Double confidence = forecast.getConfidenceScore();
        String method = forecast.getForecastMethod();
        LocalDateTime predictedStockOut = forecast.getPredictedStockOutDate();

        if (confidence != null && confidence > 0.8) {
            return String.format("High-confidence (%s) %s forecast indicates %s", confidence, method,
                    predictedStockOut != null ? "potential stock-out soon" : "stable inventory levels");
        } else if (confidence != null && confidence > 0.5) {
            return String.format("Moderate-confidence (%s) %s forecast with partial reliability", confidence, method);
        } else {
            return "Low-confidence forecast; recommend manual review before action.";
        }
    }

    @Override
    public String detectAnomaly(InventoryItem item) {
        if (item == null) {
            return "No inventory item available for anomaly detection.";
        }

        Integer quantity = item.getAvailableQuantity();
        Integer reorderLevel = item.getReorderLevel();

        if (quantity != null && reorderLevel != null) {
            if (quantity <= 0) {
                return "CRITICAL: Item is out of stock (quantity: 0).";
            } else if (quantity <= reorderLevel * 0.5) {
                return "WARNING: Stock level critically low (below 50% of reorder level).";
            } else if (quantity <= reorderLevel) {
                return "ATTENTION: Stock level at or below reorder level.";
            } else if (quantity >= reorderLevel * 3) {
                return "NOTE: Stock level significantly above reorder level; consider review.";
            }
        }

        return "No anomalies detected; inventory levels within normal range.";
    }

    @Override
    public String forecastInterpretation(DemandForecast forecast) {
        if (forecast == null) {
            return "No forecast data available for interpretation.";
        }

        Double confidence = forecast.getConfidenceScore();
        LocalDateTime predictedStockOut = forecast.getPredictedStockOutDate();
        Integer predictedDemand = forecast.getPredictedDemand();

        StringBuilder interpretation = new StringBuilder();

        if (predictedDemand != null && predictedDemand > 0) {
            interpretation.append(String.format("Predicted demand: %d units. ", predictedDemand));
        }

        if (confidence != null) {
            interpretation.append(String.format("Confidence score: %.2f. ", confidence));
        }

        if (predictedStockOut != null) {
            interpretation.append(String.format("Predicted stock-out date: %s. ", predictedStockOut));
        }

        if (interpretation.length() == 0) {
            return "Insufficient forecast data for interpretation.";
        }

        return interpretation.toString();
    }

    @Override
    public String operationalRecommendation(InventoryItem item, String forecastMethod) {
        if (item == null) {
            return "No inventory item available for recommendation.";
        }

        Integer quantity = item.getAvailableQuantity();
        Integer reorderLevel = item.getReorderLevel();

        if (quantity == null || reorderLevel == null) {
            return "Cannot generate recommendation: missing quantity or reorder level data.";
        }

        StringBuilder recommendation = new StringBuilder();

        if (quantity <= 0) {
            recommendation.append("IMMEDIATE: Place emergency order for ").append(item.getName()).append("; ");
        } else if (quantity <= reorderLevel * 0.5) {
            recommendation.append("URGENT: Place order immediately for ").append(item.getName()).append("; ");
        } else if (quantity <= reorderLevel) {
            recommendation.append("SOON: Schedule order for ").append(item.getName()).append("; ");
        } else {
            recommendation.append("MONITOR: Continue monitoring ").append(item.getName()).append(" stock levels; ");
        }

        recommendation.append("Forecast method used: ").append(forecastMethod != null ? forecastMethod : "unknown");

        return recommendation.toString();
    }

    @Override
    public Double confidenceScoreInterpretation(Double confidence) {
        if (confidence == null) {
            return 0.0;
        }

        if (confidence >= 0.9) {
            return 1.0; // Very high confidence
        } else if (confidence >= 0.75) {
            return 0.8; // High confidence
        } else if (confidence >= 0.5) {
            return 0.6; // Moderate confidence
        } else if (confidence > 0) {
            return 0.3; // Low confidence
        } else {
            return 0.0; // Invalid confidence
        }
    }

    @Override
    public boolean validateForecastOutput(DemandForecast forecast) {
        if (forecast == null) {
            return false;
        }

        // Validate required fields
        boolean hasItem = forecast.getItem() != null;
        boolean hasMethod = forecast.getForecastMethod() != null && !forecast.getForecastMethod().isEmpty();
        boolean hasPredictedDemand = forecast.getPredictedDemand() != null && forecast.getPredictedDemand() >= 0;
        boolean hasConfidence = forecast.getConfidenceScore() != null && forecast.getConfidenceScore() >= 0 && forecast.getConfidenceScore() <= 1;

        // At minimum, need forecast method and confidence
        if (!hasMethod || !hasConfidence) {
            return false;
        }

        // If predicted demand is set, it must be non-negative
        if (forecast.getPredictedDemand() != null && forecast.getPredictedDemand() < 0) {
            return false;
        }

        return hasItem && hasMethod && hasConfidence;
    }
}