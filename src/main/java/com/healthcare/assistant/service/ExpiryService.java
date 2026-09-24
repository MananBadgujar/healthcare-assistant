package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.ExpiryAlertDto;
import com.healthcare.assistant.entity.InventoryBatch;
import com.healthcare.assistant.entity.InventoryItem;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpiryService {
    List<ExpiryAlertDto> checkNearExpiry(int daysThreshold);
    List<ExpiryAlertDto> checkExpired();
    List<ExpiryAlertDto> checkByItemId(Long itemId);
    boolean isExpiryValid(LocalDateTime expiryDate);
}