package com.healthcare.assistant.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class StockOutRequest {
    @NotNull(message = "Batch number must not be null")
    private String batchNumber;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String reason;

    private LocalDateTime timestamp;

    public StockOutRequest() {}

    public StockOutRequest(String batchNumber, Integer quantity, String reason) {
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}