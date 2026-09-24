package com.healthcare.appointmentservice.repo;

import com.healthcare.appointmentservice.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByIdempotencyKey(String key);
}
