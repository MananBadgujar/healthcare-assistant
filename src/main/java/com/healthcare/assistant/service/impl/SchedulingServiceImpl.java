package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Clinic;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.entity.SchedulingSlot;
import com.healthcare.assistant.repository.SchedulingSlotRepository;
import com.healthcare.assistant.service.SchedulingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired
    private SchedulingSlotRepository slotRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<SchedulingSlot> getAvailableSlots(Provider provider, LocalDateTime start, LocalDateTime end, Integer durationMinutes) {
        return slotRepository.findByProviderIdAndSlotDateTimeBetweenAndStatusOrderBySlotDateTimeAsc(
                provider.getId(), start, end, "AVAILABLE");
    }

    @Override
    @Transactional
    public List<SchedulingSlot> getAvailableSlotsByFacility(Clinic facility, LocalDateTime start, LocalDateTime end, Integer durationMinutes) {
        return slotRepository.findByFacilityIdAndSlotDateTimeBetweenAndStatusOrderBySlotDateTimeAsc(
                facility.getId(), start, end, "AVAILABLE");
    }

    @Override
    public boolean isSlotAvailable(Long slotId) {
        Optional<SchedulingSlot> optional = slotRepository.findById(slotId);
        return optional.isPresent() && "AVAILABLE".equals(optional.get().getStatus());
    }

    @Override
    @Transactional
    public void markSlotBooked(Long slotId, String patientId, String reason) {
        SchedulingSlot slot = slotRepository.findById(slotId).orElseThrow();
        slot.setStatus("BOOKED");
        slot.setPatientId(patientId);
        slot.setReason(reason);
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);
    }

    @Override
    @Transactional
    public void markSlotReleased(Long slotId) {
        SchedulingSlot slot = slotRepository.findById(slotId).orElseThrow();
        slot.setStatus("AVAILABLE");
        slot.setPatientId(null);
        slot.setReason(null);
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);
    }

    @Override
    public List<SchedulingSlot> suggestOptimizedSlots(Provider provider, LocalDateTime start, LocalDateTime end, Integer durationMinutes, int maxSlots) {
        List<SchedulingSlot> available = getAvailableSlots(provider, start, end, durationMinutes);
        return available.stream()
                .limit(maxSlots)
                .collect(Collectors.toList());
    }
}