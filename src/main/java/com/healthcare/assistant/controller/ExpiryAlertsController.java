package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.ExpiryAlertDto;
import com.healthcare.assistant.service.ExpiryService;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class ExpiryAlertsController {

    private final ExpiryService expiryService;

    @Autowired
    public ExpiryAlertsController(ExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    @PostMapping("/expiry-alerts/near-expiry")
    public ResponseEntity<List<ExpiryAlertDto>> getNearExpiryAlerts(@RequestParam(defaultValue = "30") int daysThreshold) {
        List<ExpiryAlertDto> alerts = expiryService.checkNearExpiry(daysThreshold);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/expiry-alerts/expired")
    public ResponseEntity<List<ExpiryAlertDto>> getExpiredAlerts() {
        List<ExpiryAlertDto> alerts = expiryService.checkExpired();
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/expiry-alerts/by-item/{itemId}")
    public ResponseEntity<List<ExpiryAlertDto>> getAlertsByItemId(@PathVariable Long itemId) {
        List<ExpiryAlertDto> alerts = expiryService.checkByItemId(itemId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/expiry-alerts/valid/{expiryDate}")
    public ResponseEntity<Boolean> isExpiryValid(@PathVariable LocalDateTime expiryDate) {
        Boolean valid = expiryService.isExpiryValid(expiryDate);
        return ResponseEntity.ok(valid);
    }
}