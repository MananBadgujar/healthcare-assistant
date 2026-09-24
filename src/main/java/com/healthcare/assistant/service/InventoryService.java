package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.InventoryItemDto;
import com.healthcare.assistant.dto.StockInRequest;
import com.healthcare.assistant.dto.StockOutRequest;
import com.healthcare.assistant.dto.StockTransferRequest;
import com.healthcare.assistant.dto.StockAdjustmentRequest;
import com.healthcare.assistant.entity.InventoryItem;
import java.util.List;

public interface InventoryService {
    InventoryItemDto createItem(InventoryItemDto itemDto);
    InventoryItemDto getItemById(Long id);
    InventoryItemDto getItemBySku(String sku);
    List<InventoryItemDto> getAllItems();
    InventoryItemDto updateItem(Long id, InventoryItemDto itemDto);
    void deleteItem(Long id);
    List<InventoryItemDto> getLowStockItems();
    List<InventoryItemDto> getItemsByCategory(String category);

    // Stock operations
    InventoryItemDto stockIn(StockInRequest request);
    InventoryItemDto stockOut(StockOutRequest request);
    InventoryItemDto transferStock(StockTransferRequest request);
    InventoryItemDto adjustStock(StockAdjustmentRequest request);
    List<InventoryItemDto> getStockMovements();
}