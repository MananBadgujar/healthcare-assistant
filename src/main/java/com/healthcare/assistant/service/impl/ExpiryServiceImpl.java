package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.ExpiryAlertDto;
import com.healthcare.assistant.entity.InventoryBatch;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.repository.InventoryBatchRepository;
import com.healthcare.assistant.repository.InventoryItemRepository;
import com.healthcare.assistant.service.ExpiryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpiryServiceImpl implements ExpiryService {

    @Autowired
    private InventoryBatchRepository batchRepository;

    @Autowired
    private InventoryItemRepository itemRepository;

    private int getDaysThreshold() {
        return 30; // Default threshold, can be configured
    }

    @Override
    @Transactional
    public List<ExpiryAlertDto> checkNearExpiry(int daysThreshold) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);
        return batchRepository.findAll().stream()
                .filter(batch -> batch.getExpiryDate() != null)
                .filter(batch -> batch.getExpiryDate().isBefore(thresholdDate)
                        && batch.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(batch -> {
                    ExpiryAlertDto alert = new ExpiryAlertDto();
                    InventoryItem item = batch.getItem();
                    alert.setItemId(item.getId());
                    alert.setItemSku(item.getSku());
                    alert.setItemName(item.getName());
                    alert.setBatchNumber(batch.getBatchNumber());
                    alert.setExpiryDate(batch.getExpiryDate());
                    alert.setQuantity(batch.getQuantityReceived() != null ? batch.getQuantityReceived() : 0);
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), batch.getExpiryDate());
                    if (daysUntilExpiry <= 7) {
                        alert.setSeverity("CRITICAL");
                    } else if (daysUntilExpiry <= 14) {
                        alert.setSeverity("WARNING");
                    } else {
                        alert.setSeverity("SOON");
                    }
                    return alert;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ExpiryAlertDto> checkExpired() {
        return batchRepository.findAll().stream()
                .filter(batch -> batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDateTime.now()))
                .map(batch -> {
                    ExpiryAlertDto alert = new ExpiryAlertDto();
                    InventoryItem item = batch.getItem();
                    alert.setItemId(item.getId());
                    alert.setItemSku(item.getSku());
                    alert.setItemName(item.getName());
                    alert.setBatchNumber(batch.getBatchNumber());
                    alert.setExpiryDate(batch.getExpiryDate());
                    alert.setQuantity(batch.getQuantityReceived() != null ? batch.getQuantityReceived() : 0);
                    alert.setSeverity("CRITICAL");
                    return alert;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ExpiryAlertDto> checkByItemId(Long itemId) {
        return batchRepository.findByItemIdOrderByExpiryDateAsc(itemId).stream()
                .filter(batch -> batch.getExpiryDate() != null)
                .map(batch -> {
                    ExpiryAlertDto alert = new ExpiryAlertDto();
                    InventoryItem item = batch.getItem();
                    alert.setItemId(item.getId());
                    alert.setItemSku(item.getSku());
                    alert.setItemName(item.getName());
                    alert.setBatchNumber(batch.getBatchNumber());
                    alert.setExpiryDate(batch.getExpiryDate());
                    Integer quantity = batch.getQuantityReceived() != null ? batch.getQuantityReceived() : 0;
                    alert.setQuantity(quantity);
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), batch.getExpiryDate());
                    if (daysUntilExpiry <= 7) {
                        alert.setSeverity("CRITICAL");
                    } else if (daysUntilExpiry <= 30) {
                        alert.setSeverity("WARNING");
                    } else {
                        alert.setSeverity("SOON");
                    }
                    return alert;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isExpiryValid(LocalDateTime expiryDate) {
        return expiryDate != null && !expiryDate.isBefore(LocalDateTime.now());
    }
}