package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.healthcare.assistant.entity.Appointment;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    void deleteByPatientId(@Param("patientId") Long patientId);
    long countByProviderIdAndAppointmentDateTime(@Param("providerId") Long providerId, @Param("dateTime") java.time.LocalDateTime dateTime);
}
