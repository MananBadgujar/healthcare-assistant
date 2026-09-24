package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.service.AdvancedAnalyticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/advanced-analytics")
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsService analyticsService;

    @Autowired
    public AdvancedAnalyticsController(AdvancedAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/explain-trend")
    public ResponseEntity<String> explainTrend(@Valid @RequestBody DemandForecast forecast) {
        String explanation = analyticsService.explainTrend(forecast);
        return ResponseEntity.ok(explanation);
    }

    @PostMapping("/detect-anomaly")
    public ResponseEntity<String> detectAnomaly(@Valid @RequestBody InventoryItem item) {
        String anomaly = analyticsService.detectAnomaly(item);
        return ResponseEntity.ok(anomaly);
    }

    @PostMapping("/forecast-interpretation")
    public ResponseEntity<String> forecastInterpretation(@Valid @RequestBody DemandForecast forecast) {
        String interpretation = analyticsService.forecastInterpretation(forecast);
        return ResponseEntity.ok(interpretation);
    }

    @PostMapping("/operational-recommendation")
    public ResponseEntity<String> operationalRecommendation(@Valid @RequestBody InventoryItem item,
                                                          @RequestParam(name = "forecastMethod") String forecastMethod) {
        String recommendation = analyticsService.operationalRecommendation(item, forecastMethod);
        return ResponseEntity.ok(recommendation);
    }

    @PostMapping("/confidence-interpretation")
    public ResponseEntity<Double> confidenceInterpretation(@Valid @RequestBody Double confidence) {
        Double interpretation = analyticsService.confidenceScoreInterpretation(confidence);
        return ResponseEntity.ok(interpretation);
    }

    @PostMapping("/validate-forecast")
    public ResponseEntity<Boolean> validateForecast(@Valid @RequestBody DemandForecast forecast) {
        boolean valid = analyticsService.validateForecastOutput(forecast);
        return ResponseEntity.ok(valid);
    }
}