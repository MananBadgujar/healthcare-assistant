package com.healthcare.inventoryservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_items")
public class StockItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String sku;
    private String name;
    private int quantity;
    private int reorderThreshold = 10;
    private LocalDate expiryDate;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; } public void setSku(String v) { this.sku = v; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { this.quantity = v; }
    public int getReorderThreshold() { return reorderThreshold; } public void setReorderThreshold(int v) { this.reorderThreshold = v; }
    public LocalDate getExpiryDate() { return expiryDate; } public void setExpiryDate(LocalDate v) { this.expiryDate = v; }
}
