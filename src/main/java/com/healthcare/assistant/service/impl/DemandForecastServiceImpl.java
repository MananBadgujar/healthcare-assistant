package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.DemandForecastDto;
import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.repository.DemandForecastRepository;
import com.healthcare.assistant.repository.InventoryItemRepository;
import com.healthcare.assistant.service.DemandForecastService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DemandForecastServiceImpl implements DemandForecastService {

    @Autowired
    private DemandForecastRepository forecastRepository;

    @Autowired
    private InventoryItemRepository itemRepository;

    @Override
    @Transactional
    public DemandForecastDto createForecast(DemandForecastDto forecastDto) {
        InventoryItem item = itemRepository.findById(forecastDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + forecastDto.getItemId()));

        DemandForecast forecast = new DemandForecast();
        forecast.setItem(item);
        forecast.setForecastPeriodStart(forecastDto.getForecastPeriodStart());
        forecast.setForecastPeriodEnd(forecastDto.getForecastPeriodEnd());
        forecast.setPredictedDemand(forecastDto.getPredictedDemand());
        forecast.setConfidenceScore(forecastDto.getConfidenceScore());
        forecast.setPredictedStockOutDate(forecastDto.getPredictedStockOutDate());
        forecast.setForecastMethod(forecastDto.getForecastMethod());
        forecast.setNotes(forecastDto.getNotes());
        forecast.setCreatedAt(LocalDateTime.now());

        DemandForecast saved = forecastRepository.save(forecast);
        return convertToDto(saved);
    }

    @Override
    public List<DemandForecastDto> getForecastsByItemId(Long itemId) {
        return forecastRepository.findByItemIdOrderByForecastPeriodStartDesc(itemId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandForecastDto> getAllForecasts() {
        return forecastRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DemandForecastDto getLatestForecastByItemId(Long itemId) {
        return forecastRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(this::convertToDto)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<DemandForecastDto> getForecastsExpiringSoon() {
        // Get forecasts that have predicted stock-out dates within the next 30 days
        LocalDateTime threshold = LocalDateTime.now().plusDays(30);
        return forecastRepository.findByPredictedStockOutDateBefore(threshold).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DemandForecastDto convertToDto(DemandForecast entity) {
        DemandForecastDto dto = new DemandForecastDto();
        dto.setId(entity.getId());
        dto.setItemId(entity.getItem() != null ? entity.getItem().getId() : null);
        dto.setItemSku(entity.getItem() != null ? entity.getItem().getSku() : null);
        dto.setItemName(entity.getItem() != null ? entity.getItem().getName() : null);
        dto.setForecastPeriodStart(entity.getForecastPeriodStart());
        dto.setForecastPeriodEnd(entity.getForecastPeriodEnd());
        dto.setPredictedDemand(entity.getPredictedDemand());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setPredictedStockOutDate(entity.getPredictedStockOutDate());
        dto.setForecastMethod(entity.getForecastMethod());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}