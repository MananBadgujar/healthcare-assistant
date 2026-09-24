package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_batches")
@Data
public class InventoryBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String batchNumber;

    private LocalDateTime expiryDate;

    private Integer quantityAllocated;

    private Integer quantityReceived;

    private String status = "ACTIVE";

    @ManyToOne
    @JoinColumn(name = "item_id")
    private InventoryItem item;

    private LocalDateTime createdAt;

    public InventoryBatch() {
    }

    public InventoryBatch(String batchNumber, LocalDateTime expiryDate, InventoryItem item) {
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.item = item;
        this.quantityAllocated = 0;
        this.quantityReceived = 0;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }
}