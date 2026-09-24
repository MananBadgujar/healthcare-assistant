package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.DemandForecastDto;
import com.healthcare.assistant.service.DemandForecastService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class DemandForecastController {

    private final DemandForecastService forecastService;

    @Autowired
    public DemandForecastController(DemandForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping("/demand-forecast")
    public ResponseEntity<DemandForecastDto> createForecast(@Valid @RequestBody DemandForecastDto forecastDto) {
        DemandForecastDto created = forecastService.createForecast(forecastDto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/forecasts/item/{itemId}")
    public List<DemandForecastDto> getForecastsByItemId(@PathVariable Long itemId) {
        return forecastService.getForecastsByItemId(itemId);
    }

    @GetMapping("/forecasts")
    public List<DemandForecastDto> getAllForecasts() {
        return forecastService.getAllForecasts();
    }

    @GetMapping("/forecasts/latest/{itemId}")
    public DemandForecastDto getLatestForecastByItemId(@PathVariable Long itemId) {
        return forecastService.getLatestForecastByItemId(itemId);
    }

    @GetMapping("/forecasts/expiring")
    public List<DemandForecastDto> getForecastsExpiringSoon() {
        return forecastService.getForecastsExpiringSoon();
    }
}