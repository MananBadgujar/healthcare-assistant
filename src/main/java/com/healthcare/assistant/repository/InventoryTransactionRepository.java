package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByItemIdOrderByTimestampDesc(Long itemId);
    List<InventoryTransaction> findByOperationType(String operationType);
    List<InventoryTransaction> findAllByOrderByTimestampDesc();
}