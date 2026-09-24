package com.healthcare.assistant.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class StockTransferRequest {
    @NotNull(message = "From warehouse must not be null")
    private String fromWarehouse;

    @NotNull(message = "To warehouse must not be null")
    private String toWarehouse;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String batchNumber;

    private String reason;

    private LocalDateTime timestamp;

    public StockTransferRequest() {}

    public StockTransferRequest(String fromWarehouse, String toWarehouse, Integer quantity, String reason) {
        this.fromWarehouse = fromWarehouse;
        this.toWarehouse = toWarehouse;
        this.quantity = quantity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public String getFromWarehouse() { return fromWarehouse; }
    public void setFromWarehouse(String fromWarehouse) { this.fromWarehouse = fromWarehouse; }
    public String getToWarehouse() { return toWarehouse; }
    public void setToWarehouse(String toWarehouse) { this.toWarehouse = toWarehouse; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}