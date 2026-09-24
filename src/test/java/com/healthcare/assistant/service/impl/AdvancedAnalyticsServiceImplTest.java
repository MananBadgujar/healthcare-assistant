package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.service.AdvancedAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedAnalyticsServiceImplTest {

    private AdvancedAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AdvancedAnalyticsServiceImpl();
    }

    @Test
    void testExplainTrend_HighConfidence() {
        DemandForecast forecast = new DemandForecast();
        forecast.setConfidenceScore(0.9);
        forecast.setForecastMethod("STATISTICAL");
        String result = analyticsService.explainTrend(forecast);
        assertTrue(result.contains("High-confidence"));
    }

    @Test
    void testExplainTrend_ModerateConfidence() {
        DemandForecast forecast = new DemandForecast();
        forecast.setConfidenceScore(0.6);
        forecast.setForecastMethod("AI_ENHANCED");
        String result = analyticsService.explainTrend(forecast);
        assertTrue(result.contains("Moderate-confidence"));
    }

    @Test
    void testExplainTrend_LowConfidence() {
        DemandForecast forecast = new DemandForecast();
        forecast.setConfidenceScore(0.3);
        String result = analyticsService.explainTrend(forecast);
        assertTrue(result.contains("Low-confidence"));
    }

    @Test
    void testDetectAnomaly_OutOfStock() {
        InventoryItem item = new InventoryItem();
        item.setAvailableQuantity(0);
        item.setReorderLevel(10);
        String result = analyticsService.detectAnomaly(item);
        assertTrue(result.contains("CRITICAL"));
    }

    @Test
    void testDetectAnomaly_CriticalLow() {
        InventoryItem item = new InventoryItem();
        item.setAvailableQuantity(3);
        item.setReorderLevel(10);
        String result = analyticsService.detectAnomaly(item);
        assertTrue(result.contains("WARNING"));
    }

    @Test
    void testForecastInterpretation() {
        DemandForecast forecast = new DemandForecast();
        forecast.setConfidenceScore(0.85);
        forecast.setPredictedDemand(150);
        forecast.setPredictedStockOutDate(java.time.LocalDateTime.now().plusDays(7));
        String result = analyticsService.forecastInterpretation(forecast);
        assertTrue(result.contains("Predicted demand"));
        assertTrue(result.contains("Confidence score"));
    }

    @Test
    void testOperationalRecommendation_OutOfStock() {
        InventoryItem item = new InventoryItem();
        item.setAvailableQuantity(0);
        item.setReorderLevel(10);
        String result = analyticsService.operationalRecommendation(item, "STATISTICAL");
        assertTrue(result.contains("IMMEDIATE"));
    }

    @Test
    void testOperationalRecommendation_CriticalLow() {
        InventoryItem item = new InventoryItem();
        item.setAvailableQuantity(3);
        item.setReorderLevel(10);
        String result = analyticsService.operationalRecommendation(item, "STATISTICAL");
        assertTrue(result.contains("URGENT"));
    }

    @Test
    void testConfidenceScoreInterpretation_High() {
        Double result = analyticsService.confidenceScoreInterpretation(0.95);
        assertEquals(1.0, result);
    }

    @Test
    void testConfidenceScoreInterpretation_Moderate() {
        Double result = analyticsService.confidenceScoreInterpretation(0.7);
        assertEquals(0.6, result);
    }

    @Test
    void testValidateForecast_Valid() {
        DemandForecast forecast = new DemandForecast();
        InventoryItem item = new InventoryItem();
        item.setSku("TEST001");
        item.setName("Test Item");
        forecast.setItem(item);
        forecast.setForecastMethod("STATISTICAL");
        forecast.setConfidenceScore(0.85);
        forecast.setPredictedDemand(100);
        boolean result = analyticsService.validateForecastOutput(forecast);
        assertTrue(result);
    }

    @Test
    void testValidateForecast_Invalid_MissingMethod() {
        DemandForecast forecast = new DemandForecast();
        forecast.setConfidenceScore(0.85);
        boolean result = analyticsService.validateForecastOutput(forecast);
        assertFalse(result);
    }
}