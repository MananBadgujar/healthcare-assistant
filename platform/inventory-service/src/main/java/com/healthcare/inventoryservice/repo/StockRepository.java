package com.healthcare.inventoryservice.repo;

import com.healthcare.inventoryservice.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockItem, Long> {
}
