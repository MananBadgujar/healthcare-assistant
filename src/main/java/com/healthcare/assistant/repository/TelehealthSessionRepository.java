package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.TelehealthSession;
import com.healthcare.assistant.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelehealthSessionRepository extends JpaRepository<TelehealthSession, Long> {
    List<TelehealthSession> findByPatientIdOrderByScheduledStartDesc(Long patientId);
    List<TelehealthSession> findByProviderIdOrderByScheduledStartDesc(Long providerId);
    List<TelehealthSession> findByAppointmentId(Long appointmentId);
    List<TelehealthSession> findByStatusOrderByScheduledStart(String status);
    List<TelehealthSession> findByPatientIdAndStatusOrderByScheduledStartDesc(Long patientId, String status);
    List<TelehealthSession> findByProviderIdAndStatusOrderByScheduledStartDesc(Long providerId, String status);
}