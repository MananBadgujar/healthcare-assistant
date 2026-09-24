package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.AnalyticsDashboardStats;
import com.healthcare.assistant.entity.DemandForecast;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.PopulationRiskSegment;
import com.healthcare.assistant.service.AdvancedAnalyticsService;
import com.healthcare.assistant.service.AnalyticsDashboardService;
import com.healthcare.assistant.service.PopulationHealthService;
import com.healthcare.assistant.service.PopulationRiskDetectionService;

/**
 * Phase 4c/5/6 endpoint coverage: AdvancedAnalytics (6), AnalyticsDashboard (4),
 * ConfigReload (1), PopulationRisk (4).
 */
@WebMvcTest({AdvancedAnalyticsController.class, AnalyticsDashboardController.class,
        ConfigReloadController.class, PopulationRiskDetectionController.class})
@WithMockUser(roles = "USER")
class AnalyticsPopulationEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdvancedAnalyticsService analyticsService;

    @MockBean
    private AnalyticsDashboardService dashboardService;

    @MockBean
    private PopulationRiskDetectionService riskDetectionService;

    @MockBean
    private PopulationHealthService populationHealthService;

    // ---------- AdvancedAnalytics (6) ----------

    @Test
    void explainTrend_returnsOk() throws Exception {
        when(analyticsService.explainTrend(any(DemandForecast.class))).thenReturn("Trend is rising");
        mockMvc.perform(post("/api/v1/advanced-analytics/explain-trend").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Trend is rising"));
    }

    @Test
    void detectAnomaly_returnsOk() throws Exception {
        when(analyticsService.detectAnomaly(any(InventoryItem.class))).thenReturn("No anomaly");
        mockMvc.perform(post("/api/v1/advanced-analytics/detect-anomaly").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void forecastInterpretation_returnsOk() throws Exception {
        when(analyticsService.forecastInterpretation(any(DemandForecast.class))).thenReturn("Stable");
        mockMvc.perform(post("/api/v1/advanced-analytics/forecast-interpretation").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void operationalRecommendation_returnsOk() throws Exception {
        when(analyticsService.operationalRecommendation(any(InventoryItem.class), anyString()))
                .thenReturn("Reorder");
        mockMvc.perform(post("/api/v1/advanced-analytics/operational-recommendation").with(csrf())
                .param("forecastMethod", "ARIMA")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Reorder"));
    }

    @Test
    void confidenceInterpretation_returnsOk() throws Exception {
        when(analyticsService.confidenceScoreInterpretation(any())).thenReturn(0.9);
        mockMvc.perform(post("/api/v1/advanced-analytics/confidence-interpretation").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("0.9"))
                .andExpect(status().isOk());
    }

    @Test
    void validateForecast_returnsOk() throws Exception {
        when(analyticsService.validateForecastOutput(any(DemandForecast.class))).thenReturn(true);
        mockMvc.perform(post("/api/v1/advanced-analytics/validate-forecast").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    // ---------- AnalyticsDashboard (4) ----------

    @Test
    void createStats_returnsCreated() throws Exception {
        AnalyticsDashboardStats stats = new AnalyticsDashboardStats();
        when(dashboardService.createStats(any(), any(), any())).thenReturn(stats);
        mockMvc.perform(post("/api/v1/analytics/dashboard/stats").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getStatsByType_returnsOk() throws Exception {
        when(dashboardService.getStatsByType("demand"))
                .thenReturn(Collections.singletonList(new AnalyticsDashboardStats()));
        mockMvc.perform(get("/api/v1/analytics/dashboard/stats/demand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getStatsByPeriod_returnsOk() throws Exception {
        when(dashboardService.getStatsByPeriod(any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/analytics/dashboard/stats/period")
                .param("start", "2025-01-01T00:00:00")
                .param("end", "2025-02-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getOverview_returnsOk() throws Exception {
        when(dashboardService.getAllStats()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/analytics/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- ConfigReload (1) ----------

    @Test
    void reloadConfig_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/config/reload").with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ---------- PopulationRisk (4) ----------

    @Test
    void detectRisksByReport_returnsOk() throws Exception {
        when(populationHealthService.getReportById(1L)).thenReturn(new PopulationHealthReport());
        when(riskDetectionService.detectRisksByReport(any(PopulationHealthReport.class)))
                .thenReturn(Collections.singletonList(new PopulationRiskSegment()));
        mockMvc.perform(post("/api/v1/population-risk/detect/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void detectRisksByReport_missing_returnsNotFound() throws Exception {
        when(populationHealthService.getReportById(999L)).thenReturn(null);
        mockMvc.perform(post("/api/v1/population-risk/detect/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void detectEmergingRisks_returnsOk() throws Exception {
        when(riskDetectionService.detectEmergingRisks(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(post("/api/v1/population-risk/detect/emerging").with(csrf())
                .param("fromDate", "2025-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void detectByRiskCategory_returnsOk() throws Exception {
        when(riskDetectionService.detectByRiskCategory("CARDIAC"))
                .thenReturn(Collections.singletonList(new PopulationRiskSegment()));
        mockMvc.perform(get("/api/v1/population-risk/category/CARDIAC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void assessPatientRisk_returnsOk() throws Exception {
        when(riskDetectionService.assessPatientRisk(anyLong(), any()))
                .thenReturn(new PopulationRiskSegment());
        mockMvc.perform(post("/api/v1/population-risk/assess/1").with(csrf())
                .param("date", "2025-01-01T00:00:00"))
                .andExpect(status().isOk());
    }
}
