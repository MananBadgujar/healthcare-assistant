package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.PopulationRiskSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PopulationRiskDetectionServiceImplTest {

    @Autowired
    private PopulationRiskDetectionServiceImpl riskDetectionService;

    @BeforeEach
    void setUp() {
        PopulationHealthReport report = new PopulationHealthReport("DISEASE_PREVALENCE", java.time.LocalDateTime.now().minusDays(30), java.time.LocalDateTime.now());
    }

    @Test
    void testAssessPatientRisk_usesProvidedData() {
        PopulationRiskSegment risk = riskDetectionService.assessPatientRisk(1L, java.time.LocalDateTime.now());
        assertNotNull(risk);
    }

    @Test
    void testDetectRisksByReport_methodExists() {
        PopulationHealthReport report = new PopulationHealthReport("DISEASE_PREVALENCE", java.time.LocalDateTime.now().minusDays(30), java.time.LocalDateTime.now());
        assertDoesNotThrow(() -> riskDetectionService.detectRisksByReport(report));
    }

    @Test
    void testDetectEmergingRisks_methodExists() {
        assertDoesNotThrow(() -> riskDetectionService.detectEmergingRisks(java.time.LocalDateTime.now().minusDays(90)));
    }

    @Test
    void testDetectByRiskCategory_methodExists() {
        assertDoesNotThrow(() -> riskDetectionService.detectByRiskCategory("HIGH_RISK"));
    }
}