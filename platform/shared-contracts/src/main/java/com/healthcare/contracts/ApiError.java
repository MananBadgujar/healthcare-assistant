package com.healthcare.contracts;

import java.time.Instant;

/** Centralized API error model used by gateway and every service. */
public class ApiError {
    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String correlationId;
    private String service;

    public ApiError() {}

    public ApiError(int status, String code, String message, String correlationId, String service) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.code = code;
        this.message = message;
        this.correlationId = correlationId;
        this.service = service;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}
