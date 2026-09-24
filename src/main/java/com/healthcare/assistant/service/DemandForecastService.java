package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.DemandForecastDto;
import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import java.util.List;

public interface DemandForecastService {
    DemandForecastDto createForecast(DemandForecastDto forecastDto);
    List<DemandForecastDto> getForecastsByItemId(Long itemId);
    List<DemandForecastDto> getAllForecasts();
    DemandForecastDto getLatestForecastByItemId(Long itemId);
    List<DemandForecastDto> getForecastsExpiringSoon();
}