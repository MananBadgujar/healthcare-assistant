package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Data
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    private String name;

    private String category;

    private Integer availableQuantity = 0;

    private Integer reservedQuantity = 0;

    private Integer reorderLevel;

    private Integer minimumStock = 0;

    private Integer maximumStock;

    private String unitOfMeasurement;

    private String supplier;

    private String warehouseLocation;

    private String status = "ACTIVE";

    private String batchNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public InventoryItem() {
    }

    public InventoryItem(String sku, String name, String category, String unitOfMeasurement) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unitOfMeasurement = unitOfMeasurement;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}