package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.PopulationRiskSegment;
import com.healthcare.assistant.entity.PopulationHealthReport;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PopulationRiskSegmentRepository extends JpaRepository<PopulationRiskSegment, Long> {

    @Query("SELECT prs FROM PopulationRiskSegment prs WHERE prs.riskCategory = ?1 ORDER BY prs.detectedAt DESC")
    List<PopulationRiskSegment> findByRiskCategoryOrderByDetectedDesc(String riskCategory);

    List<PopulationRiskSegment> findByReportId(Long reportId);

    @Query("SELECT prs FROM PopulationRiskSegment prs WHERE prs.riskCategory = :category AND prs.isActive = true ORDER BY prs.riskScore DESC")
    List<PopulationRiskSegment> findActiveByRiskCategory(@Param("category") String riskCategory);

    @Query("SELECT prs FROM PopulationRiskSegment prs WHERE prs.report = :report")
    List<PopulationRiskSegment> findByReport(@Param("report") PopulationHealthReport report);

    List<PopulationRiskSegment> findByRiskCategoryAndPatientCountGreaterThan(String riskCategory, Integer minPatients);

    @Query("SELECT prs FROM PopulationRiskSegment prs WHERE prs.detectedAt >= :fromDate AND prs.isActive = true ORDER BY prs.riskScore DESC")
List<PopulationRiskSegment> findEmergingRisks(@Param("fromDate") LocalDateTime fromDate);
}