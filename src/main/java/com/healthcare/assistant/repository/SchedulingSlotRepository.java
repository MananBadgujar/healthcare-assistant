package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.SchedulingSlot;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulingSlotRepository extends JpaRepository<SchedulingSlot, Long> {
    List<SchedulingSlot> findByProviderIdAndSlotDateTimeBetweenAndStatusOrderBySlotDateTimeAsc(
            Long providerId,
            LocalDateTime start,
            LocalDateTime end,
            String status);

    List<SchedulingSlot> findByFacilityIdAndSlotDateTimeBetweenAndStatusOrderBySlotDateTimeAsc(
            Long facilityId,
            LocalDateTime start,
            LocalDateTime end,
            String status);

    List<SchedulingSlot> findByProviderIdAndSlotDateTimeAfterOrderBySlotDateTimeAsc(Long providerId, LocalDateTime after);

    @Query("SELECT ss FROM SchedulingSlot ss WHERE ss.provider.id IN ?1 AND ss.slotDateTime BETWEEN ?2 AND ?3 AND ss.status = 'AVAILABLE'")
    List<SchedulingSlot> findAvailableByProviderIdInDateRange(List<Long> providerIds, LocalDateTime start, LocalDateTime end);
}