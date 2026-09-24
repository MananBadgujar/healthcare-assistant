package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Clinic;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.entity.SchedulingSlot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SchedulingService {
    List<SchedulingSlot> getAvailableSlots(Provider provider, LocalDateTime start, LocalDateTime end, Integer durationMinutes);
    List<SchedulingSlot> getAvailableSlotsByFacility(Clinic facility, LocalDateTime start, LocalDateTime end, Integer durationMinutes);
    boolean isSlotAvailable(Long slotId);
    void markSlotBooked(Long slotId, String patientId, String reason);
    void markSlotReleased(Long slotId);
    List<SchedulingSlot> suggestOptimizedSlots(Provider provider, LocalDateTime start, LocalDateTime end, Integer durationMinutes, int maxSlots);
}