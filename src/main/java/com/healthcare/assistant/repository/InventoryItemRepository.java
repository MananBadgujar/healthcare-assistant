package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findBySku(String sku);
    List<InventoryItem> findByCategory(String category);
    List<InventoryItem> findByStatus(String status);
    InventoryItem findByBatchNumber(String batchNumber);
    InventoryItem findByWarehouseLocation(String warehouseLocation);
}