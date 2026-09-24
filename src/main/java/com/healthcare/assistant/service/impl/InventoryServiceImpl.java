package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.InventoryItemDto;
import com.healthcare.assistant.dto.StockInRequest;
import com.healthcare.assistant.dto.StockOutRequest;
import com.healthcare.assistant.dto.StockTransferRequest;
import com.healthcare.assistant.dto.StockAdjustmentRequest;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.entity.InventoryBatch;
import com.healthcare.assistant.entity.InventoryTransaction;
import com.healthcare.assistant.repository.InventoryItemRepository;
import com.healthcare.assistant.repository.InventoryBatchRepository;
import com.healthcare.assistant.repository.InventoryTransactionRepository;
import com.healthcare.assistant.service.InventoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private InventoryBatchRepository batchRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Override
    @Transactional
    public InventoryItemDto createItem(InventoryItemDto itemDto) {
        InventoryItem item = new InventoryItem();
        item.setSku(itemDto.getSku());
        item.setName(itemDto.getName());
        item.setCategory(itemDto.getCategory());
        item.setAvailableQuantity(itemDto.getAvailableQuantity() != null ? itemDto.getAvailableQuantity() : 0);
        item.setReservedQuantity(itemDto.getReservedQuantity() != null ? itemDto.getReservedQuantity() : 0);
        item.setReorderLevel(itemDto.getReorderLevel());
        item.setMinimumStock(itemDto.getMinimumStock() != null ? itemDto.getMinimumStock() : 0);
        item.setMaximumStock(itemDto.getMaximumStock());
        item.setUnitOfMeasurement(itemDto.getUnitOfMeasurement());
        item.setSupplier(itemDto.getSupplier());
        item.setWarehouseLocation(itemDto.getWarehouseLocation());
        item.setStatus(itemDto.getStatus());
        item.setBatchNumber(itemDto.getBatchNumber());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        InventoryItem saved = itemRepository.save(item);

        // Create initial batch if batch number provided
        if (saved.getBatchNumber() != null && !saved.getBatchNumber().isEmpty()) {
            InventoryBatch batch = new InventoryBatch();
            batch.setBatchNumber(saved.getBatchNumber());
            batch.setItem(saved);
            batch.setQuantityReceived(saved.getAvailableQuantity());
            batch.setStatus("ACTIVE");
            batchRepository.save(batch);
        }

        return convertToDto(saved);
    }

    @Override
    public InventoryItemDto getItemById(Long id) {
        return itemRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public InventoryItemDto getItemBySku(String sku) {
        return itemRepository.findBySku(sku)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<InventoryItemDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryItemDto updateItem(Long id, InventoryItemDto itemDto) {
        return itemRepository.findById(id).map(existing -> {
            existing.setName(itemDto.getName() != null ? itemDto.getName() : existing.getName());
            existing.setCategory(itemDto.getCategory() != null ? itemDto.getCategory() : existing.getCategory());
            if (itemDto.getAvailableQuantity() != null) {
                existing.setAvailableQuantity(itemDto.getAvailableQuantity());
            }
            if (itemDto.getReservedQuantity() != null) {
                existing.setReservedQuantity(itemDto.getReservedQuantity());
            }
            existing.setReorderLevel(itemDto.getReorderLevel() != null ? itemDto.getReorderLevel() : existing.getReorderLevel());
            existing.setMinimumStock(itemDto.getMinimumStock() != null ? itemDto.getMinimumStock() : existing.getMinimumStock());
            existing.setMaximumStock(itemDto.getMaximumStock() != null ? itemDto.getMaximumStock() : existing.getMaximumStock());
            existing.setUnitOfMeasurement(itemDto.getUnitOfMeasurement() != null ? itemDto.getUnitOfMeasurement() : existing.getUnitOfMeasurement());
            existing.setSupplier(itemDto.getSupplier() != null ? itemDto.getSupplier() : existing.getSupplier());
            existing.setWarehouseLocation(itemDto.getWarehouseLocation() != null ? itemDto.getWarehouseLocation() : existing.getWarehouseLocation());
            existing.setStatus(itemDto.getStatus() != null ? itemDto.getStatus() : existing.getStatus());
            existing.setBatchNumber(itemDto.getBatchNumber() != null ? itemDto.getBatchNumber() : existing.getBatchNumber());
            existing.setUpdatedAt(LocalDateTime.now());

            InventoryItem updated = itemRepository.save(existing);
            return convertToDto(updated);
        }).orElse(null);
    }

    @Override
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<InventoryItemDto> getLowStockItems() {
        // Find items where available quantity is less than or equal to reorder level
        return itemRepository.findAll().stream()
                .filter(item -> item.getAvailableQuantity() != null && item.getReorderLevel() != null)
                .filter(item -> item.getAvailableQuantity() <= item.getReorderLevel())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItemDto> getItemsByCategory(String category) {
        return itemRepository.findByCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryItemDto stockIn(StockInRequest request) {
        // Find item by batch number
        InventoryItem item = null;
        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            item = itemRepository.findByBatchNumber(request.getBatchNumber());
        }
        if (item == null) {
            // Create new item if not exists
            item = new InventoryItem();
            item.setSku(request.getBatchNumber());
            item.setName("Item from stock-in");
            item.setBatchNumber(request.getBatchNumber());
            item.setAvailableQuantity(0);
            item.setReservedQuantity(0);
            item.setStatus("ACTIVE");
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item = itemRepository.save(item);
        }

        // Add stock
        Integer currentAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
        item.setAvailableQuantity(currentAvailable + request.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());
        InventoryItem updatedItem = itemRepository.save(item);

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(updatedItem);
        transaction.setOperationType("STOCK_IN");
        transaction.setQuantity(request.getQuantity());
        transaction.setBatchNumber(request.getBatchNumber());
        transaction.setReason(request.getReason());
        transaction.setActorId(1L); // TODO: get from security context
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        return convertToDto(updatedItem);
    }

    @Override
    @Transactional
    public InventoryItemDto stockOut(StockOutRequest request) {
        // Find item by batch number
        InventoryItem item = null;
        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            item = itemRepository.findByBatchNumber(request.getBatchNumber());
        }
        if (item == null) {
            return null;
        }

        // Check available stock
        Integer currentAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
        if (currentAvailable < request.getQuantity()) {
            // Insufficient stock - cannot go negative unless policy allows
            return null;
        }

        // Deduct stock
        item.setAvailableQuantity(currentAvailable - request.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());
        InventoryItem updatedItem = itemRepository.save(item);

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(updatedItem);
        transaction.setOperationType("STOCK_OUT");
        transaction.setQuantity(request.getQuantity());
        transaction.setBatchNumber(request.getBatchNumber());
        transaction.setReason(request.getReason());
        transaction.setActorId(1L); // TODO: get from security context
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        return convertToDto(updatedItem);
    }

    @Override
    @Transactional
    public InventoryItemDto transferStock(StockTransferRequest request) {
        // Find source item
        InventoryItem sourceItem = null;
        if (request.getFromWarehouse() != null) {
            sourceItem = itemRepository.findByWarehouseLocation(request.getFromWarehouse());
        }
        if (sourceItem == null) {
            return null;
        }

        // Check available stock (available - reserved)
        Integer availableStock = sourceItem.getAvailableQuantity() - sourceItem.getReservedQuantity();
        if (availableStock < request.getQuantity()) {
            return null;
        }

        // Deduct from source
        sourceItem.setAvailableQuantity(sourceItem.getAvailableQuantity() - request.getQuantity());
        sourceItem.setUpdatedAt(LocalDateTime.now());
        sourceItem = itemRepository.save(sourceItem);

        // Create transaction for source
        InventoryTransaction sourceTransaction = new InventoryTransaction();
        sourceTransaction.setItem(sourceItem);
        sourceTransaction.setOperationType("TRANSFER_OUT");
        sourceTransaction.setQuantity(request.getQuantity());
        sourceTransaction.setBatchNumber(request.getBatchNumber());
        sourceTransaction.setSourceWarehouse(request.getFromWarehouse());
        sourceTransaction.setReason(request.getReason());
        sourceTransaction.setActorId(1L); // TODO: get from security context
        sourceTransaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(sourceTransaction);

        // Find or create destination item
        InventoryItem destItem = null;
        if (request.getToWarehouse() != null) {
            destItem = itemRepository.findByWarehouseLocation(request.getToWarehouse());
        }
        if (destItem == null) {
            destItem = new InventoryItem();
            destItem.setWarehouseLocation(request.getToWarehouse());
            destItem.setSku(sourceItem.getSku());
            destItem.setName(sourceItem.getName());
            destItem.setAvailableQuantity(0);
            destItem.setReservedQuantity(0);
            destItem.setStatus("ACTIVE");
            destItem.setCreatedAt(LocalDateTime.now());
            destItem.setUpdatedAt(LocalDateTime.now());
            destItem = itemRepository.save(destItem);
        }

        // Add to destination
        Integer destAvailable = destItem.getAvailableQuantity() != null ? destItem.getAvailableQuantity() : 0;
        destItem.setAvailableQuantity(destAvailable + request.getQuantity());
        destItem.setUpdatedAt(LocalDateTime.now());
        destItem = itemRepository.save(destItem);

        // Create transaction for destination
        InventoryTransaction destTransaction = new InventoryTransaction();
        destTransaction.setItem(destItem);
        destTransaction.setOperationType("TRANSFER_IN");
        destTransaction.setQuantity(request.getQuantity());
        destTransaction.setBatchNumber(request.getBatchNumber());
        destTransaction.setDestinationWarehouse(request.getToWarehouse());
        destTransaction.setReason(request.getReason());
        destTransaction.setActorId(1L); // TODO: get from security context
        destTransaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(destTransaction);

        return convertToDto(destItem);
    }

    @Override
    @Transactional
    public InventoryItemDto adjustStock(StockAdjustmentRequest request) {
        // Find item by batch number
        InventoryItem item = null;
        if (request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()) {
            item = itemRepository.findByBatchNumber(request.getBatchNumber());
        }
        if (item == null) {
            return null;
        }

        Integer currentAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
        Integer newQuantity = currentAvailable + request.getQuantity();

        // Check that we don't go negative (unless policy allows)
        if (newQuantity < 0) {
            return null;
        }

        item.setAvailableQuantity(newQuantity);
        item.setUpdatedAt(LocalDateTime.now());
        InventoryItem updatedItem = itemRepository.save(item);

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(updatedItem);
        transaction.setOperationType("ADJUSTMENT");
        transaction.setQuantity(request.getQuantity());
        transaction.setBatchNumber(request.getBatchNumber());
        transaction.setReason(request.getReason());
        transaction.setActorId(1L); // TODO: get from security context
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        return convertToDto(updatedItem);
    }

    @Override
    public List<InventoryItemDto> getStockMovements() {
        return transactionRepository.findAllByOrderByTimestampDesc().stream()
                .map(transaction -> {
                    InventoryItemDto dto = new InventoryItemDto();
                    dto.setId(transaction.getId());
                    // Can't directly map item here without loading the item
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private InventoryItemDto convertToDto(InventoryItem entity) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(entity.getId());
        dto.setSku(entity.getSku());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        dto.setAvailableQuantity(entity.getAvailableQuantity());
        dto.setReservedQuantity(entity.getReservedQuantity());
        dto.setReorderLevel(entity.getReorderLevel());
        dto.setMinimumStock(entity.getMinimumStock());
        dto.setMaximumStock(entity.getMaximumStock());
        dto.setUnitOfMeasurement(entity.getUnitOfMeasurement());
        dto.setSupplier(entity.getSupplier());
        dto.setWarehouseLocation(entity.getWarehouseLocation());
        dto.setStatus(entity.getStatus());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}