package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.healthcare.assistant.dto.DemandForecastDto;
import com.healthcare.assistant.dto.ExpiryAlertDto;
import com.healthcare.assistant.dto.InventoryItemDto;
import com.healthcare.assistant.dto.StockAdjustmentRequest;
import com.healthcare.assistant.dto.StockInRequest;
import com.healthcare.assistant.dto.StockOutRequest;
import com.healthcare.assistant.dto.StockTransferRequest;
import com.healthcare.assistant.service.DemandForecastService;
import com.healthcare.assistant.service.ExpiryService;
import com.healthcare.assistant.service.InventoryService;

/**
 * Phase 3c endpoint coverage: Inventory (13) + DemandForecast (5) + ExpiryAlerts (4).
 */
@WebMvcTest({InventoryController.class, DemandForecastController.class, ExpiryAlertsController.class})
@WithMockUser(roles = "USER")
class InventoryEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private DemandForecastService forecastService;

    @MockBean
    private ExpiryService expiryService;

    private InventoryItemDto item() {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(1L);
        dto.setSku("SKU-1");
        dto.setName("Bandage");
        return dto;
    }

    // ---------- Inventory (13) ----------

    @Test
    void createItem_returnsCreated() throws Exception {
        when(inventoryService.createItem(any(InventoryItemDto.class))).thenReturn(item());
        mockMvc.perform(post("/api/v1/inventory").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"SKU-1\",\"name\":\"Bandage\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    void getItem_returnsOk() throws Exception {
        when(inventoryService.getItemById(1L)).thenReturn(item());
        mockMvc.perform(get("/api/v1/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    void getItem_missing_returnsNotFound() throws Exception {
        when(inventoryService.getItemById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/v1/inventory/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemBySku_returnsOk() throws Exception {
        when(inventoryService.getItemBySku("SKU-1")).thenReturn(item());
        mockMvc.perform(get("/api/v1/inventory/sku/SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bandage"));
    }

    @Test
    void getAllItems_returnsOk() throws Exception {
        when(inventoryService.getAllItems()).thenReturn(Collections.singletonList(item()));
        mockMvc.perform(get("/api/v1/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateItem_returnsOk() throws Exception {
        when(inventoryService.updateItem(anyLong(), any(InventoryItemDto.class))).thenReturn(item());
        mockMvc.perform(put("/api/v1/inventory/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"SKU-1\",\"name\":\"Bandage\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/inventory/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void stockIn_returnsOk() throws Exception {
        when(inventoryService.stockIn(any(StockInRequest.class))).thenReturn(item());
        mockMvc.perform(post("/api/v1/inventory/stock-in").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"batchNumber\":\"B-1\",\"quantity\":10}"))
                .andExpect(status().isOk());
    }

    @Test
    void stockIn_invalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/stock-in").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"batchNumber\":\"B-1\",\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockOut_returnsOk() throws Exception {
        when(inventoryService.stockOut(any(StockOutRequest.class))).thenReturn(item());
        mockMvc.perform(post("/api/v1/inventory/stock-out").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"batchNumber\":\"B-1\",\"quantity\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    void transfer_returnsOk() throws Exception {
        when(inventoryService.transferStock(any(StockTransferRequest.class))).thenReturn(item());
        mockMvc.perform(post("/api/v1/inventory/transfer").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromWarehouse\":\"W1\",\"toWarehouse\":\"W2\",\"quantity\":3}"))
                .andExpect(status().isOk());
    }

    @Test
    void adjust_returnsOk() throws Exception {
        when(inventoryService.adjustStock(any(StockAdjustmentRequest.class))).thenReturn(item());
        mockMvc.perform(post("/api/v1/inventory/adjust").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":5,\"reason\":\"recount\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getMovements_returnsOk() throws Exception {
        when(inventoryService.getStockMovements()).thenReturn(Collections.singletonList(item()));
        mockMvc.perform(get("/api/v1/inventory/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getLowStock_returnsOk() throws Exception {
        when(inventoryService.getLowStockItems()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getByCategory_returnsOk() throws Exception {
        when(inventoryService.getItemsByCategory("consumables")).thenReturn(Collections.singletonList(item()));
        mockMvc.perform(get("/api/v1/inventory/by-category/consumables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DemandForecast (5) ----------

    @Test
    void createForecast_returnsCreated() throws Exception {
        DemandForecastDto dto = new DemandForecastDto();
        dto.setId(1L);
        when(forecastService.createForecast(any(DemandForecastDto.class))).thenReturn(dto);
        mockMvc.perform(post("/api/v1/inventory/demand-forecast").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getForecastsByItem_returnsOk() throws Exception {
        when(forecastService.getForecastsByItemId(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/inventory/forecasts/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllForecasts_returnsOk() throws Exception {
        when(forecastService.getAllForecasts()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/inventory/forecasts"))
                .andExpect(status().isOk());
    }

    @Test
    void getLatestForecast_returnsOk() throws Exception {
        when(forecastService.getLatestForecastByItemId(1L)).thenReturn(new DemandForecastDto());
        mockMvc.perform(get("/api/v1/inventory/forecasts/latest/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getExpiringForecasts_returnsOk() throws Exception {
        when(forecastService.getForecastsExpiringSoon()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/inventory/forecasts/expiring"))
                .andExpect(status().isOk());
    }

    // ---------- ExpiryAlerts (4) ----------

    @Test
    void nearExpiry_returnsOk() throws Exception {
        when(expiryService.checkNearExpiry(anyInt())).thenReturn(Collections.emptyList());
        mockMvc.perform(post("/api/v1/inventory/expiry-alerts/near-expiry").with(csrf())
                .param("daysThreshold", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void expiredAlerts_returnsOk() throws Exception {
        ExpiryAlertDto alert = new ExpiryAlertDto();
        when(expiryService.checkExpired()).thenReturn(Collections.singletonList(alert));
        mockMvc.perform(post("/api/v1/inventory/expiry-alerts/expired").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void alertsByItem_returnsOk() throws Exception {
        when(expiryService.checkByItemId(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(post("/api/v1/inventory/expiry-alerts/by-item/1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void isExpiryValid_returnsOk() throws Exception {
        when(expiryService.isExpiryValid(any())).thenReturn(true);
        mockMvc.perform(get("/api/v1/inventory/expiry-alerts/valid/2030-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void unusedStringStubCoverage() throws Exception {
        when(inventoryService.getItemBySku(anyString())).thenReturn(item());
        mockMvc.perform(get("/api/v1/inventory/sku/OTHER"))
                .andExpect(status().isOk());
    }
}
