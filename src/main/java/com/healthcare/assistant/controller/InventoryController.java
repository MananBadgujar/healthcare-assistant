package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.InventoryItemDto;
import com.healthcare.assistant.dto.StockInRequest;
import com.healthcare.assistant.dto.StockOutRequest;
import com.healthcare.assistant.dto.StockTransferRequest;
import com.healthcare.assistant.dto.StockAdjustmentRequest;
import com.healthcare.assistant.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryItemDto> createItem(@Valid @RequestBody InventoryItemDto itemDto) {
        InventoryItemDto created = inventoryService.createItem(itemDto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> getItem(@PathVariable Long id) {
        InventoryItemDto item = inventoryService.getItemById(id);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryItemDto> getItemBySku(@PathVariable String sku) {
        InventoryItemDto item = inventoryService.getItemBySku(sku);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<InventoryItemDto> getAllItems() {
        return inventoryService.getAllItems();
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateItem(@PathVariable Long id, @Valid @RequestBody InventoryItemDto itemDto) {
        InventoryItemDto updated = inventoryService.updateItem(id, itemDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stock-in")
    public ResponseEntity<InventoryItemDto> stockIn(@Valid @RequestBody StockInRequest request) {
        return ResponseEntity.ok(inventoryService.stockIn(request));
    }

    @PostMapping("/stock-out")
    public ResponseEntity<InventoryItemDto> stockOut(@Valid @RequestBody StockOutRequest request) {
        InventoryItemDto result = inventoryService.stockOut(request);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.badRequest().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<InventoryItemDto> transfer(@Valid @RequestBody StockTransferRequest request) {
        InventoryItemDto result = inventoryService.transferStock(request);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.badRequest().build();
    }

    @PostMapping("/adjust")
    public ResponseEntity<InventoryItemDto> adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        InventoryItemDto result = inventoryService.adjustStock(request);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.badRequest().build();
    }

    @GetMapping("/movements")
    public List<InventoryItemDto> getMovements() {
        return inventoryService.getStockMovements();
    }

    @GetMapping("/low-stock")
    public List<InventoryItemDto> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }

    @GetMapping("/by-category/{category}")
    public List<InventoryItemDto> getItemsByCategory(@PathVariable String category) {
        return inventoryService.getItemsByCategory(category);
    }
}