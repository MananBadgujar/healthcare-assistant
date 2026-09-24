package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Column(nullable = false)
    private String operationType; // STOCK_IN, STOCK_OUT, TRANSFER, ADJUSTMENT, RESERVATION, RELEASE, DAMAGED, LOST

    @Column(nullable = false)
    private Integer quantity;

    private String sourceWarehouse;

    private String destinationWarehouse;

    private String batchNumber;

    private String reason;

    private LocalDateTime timestamp;

    private Long actorId;

    public InventoryTransaction() {
    }

    public InventoryTransaction(InventoryItem item, String operationType, Integer quantity, String batchNumber, String reason, Long actorId) {
        this.item = item;
        this.operationType = operationType;
        this.quantity = quantity;
        this.batchNumber = batchNumber;
        this.reason = reason;
        this.actorId = actorId;
        this.timestamp = LocalDateTime.now();
    }
}