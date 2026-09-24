package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandForecastRepository extends JpaRepository<DemandForecast, Long> {
    List<DemandForecast> findByItemIdOrderByForecastPeriodStartDesc(Long itemId);
    List<DemandForecast> findByPredictedStockOutDateBefore(LocalDateTime date);
    List<DemandForecast> findByItemIdOrderByCreatedAtDesc(Long itemId);
}