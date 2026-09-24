package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.Patient;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PopulationHealthReportRepository extends JpaRepository<PopulationHealthReport, Long> {

    List<PopulationHealthReport> findByReportTypeOrderByGeneratedAtDesc(String reportType);

    List<PopulationHealthReport> findByReportingPeriodStartAfter(LocalDateTime start);

    List<PopulationHealthReport> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT phr FROM PopulationHealthReport phr WHERE phr.reportType = :type AND phr.generatedAt >= :start AND phr.generatedAt <= :end ORDER BY phr.generatedAt DESC")
    List<PopulationHealthReport> findReportsByTypeAndPeriod(@Param("type") String type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<PopulationHealthReport> findLatestByReportType(String reportType);
}