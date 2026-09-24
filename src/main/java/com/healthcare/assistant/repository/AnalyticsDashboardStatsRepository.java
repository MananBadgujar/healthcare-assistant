package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.AnalyticsDashboardStats;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalyticsDashboardStatsRepository extends JpaRepository<AnalyticsDashboardStats, Long> {

    List<AnalyticsDashboardStats> findByStatTypeOrderByCalculatedAtDesc(String statType);

    List<AnalyticsDashboardStats> findByPeriodStartAfter(LocalDateTime start);

    List<AnalyticsDashboardStats> findByPeriodEndBefore(LocalDateTime end);

    @Query("SELECT ads FROM AnalyticsDashboardStats ads WHERE ads.statType = :type AND ads.periodStart >= :start AND ads.periodEnd <= :end ORDER BY ads.calculatedAt DESC")
    List<AnalyticsDashboardStats> findByTypeAndPeriod(@Param("type") String type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}