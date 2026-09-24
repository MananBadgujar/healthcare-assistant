package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "scheduling_slots")
@Data
public class SchedulingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Clinic facility;

    private LocalDateTime slotDateTime;

    private Integer durationMinutes;

    private String status = "AVAILABLE"; // AVAILABLE, BOOKED, CANCELLED

    private String patientId; // null if not booked

    private String reason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public SchedulingSlot() {
    }

    public SchedulingSlot(Provider provider, Clinic facility, LocalDateTime slotDateTime, Integer durationMinutes) {
        this.provider = provider;
        this.facility = facility;
        this.slotDateTime = slotDateTime;
        this.durationMinutes = durationMinutes;
        this.status = "AVAILABLE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}