package com.healthcare.assistant.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class StockAdjustmentRequest {
    @NotNull(message = "Quantity must not be null")
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    private String reason;

    private String batchNumber;

    private LocalDateTime timestamp;

    public StockAdjustmentRequest() {}

    public StockAdjustmentRequest(Integer quantity, String reason, String batchNumber) {
        this.quantity = quantity;
        this.reason = reason;
        this.batchNumber = batchNumber;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}