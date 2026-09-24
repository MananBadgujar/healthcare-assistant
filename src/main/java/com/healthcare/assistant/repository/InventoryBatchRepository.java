package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByItemIdOrderByExpiryDateAsc(Long itemId);
    List<InventoryBatch> findByExpiryDateBefore(LocalDateTime date);
    List<InventoryBatch> findByStatus(String status);
    List<InventoryBatch> findAllByOrderByExpiryDateAsc();
}