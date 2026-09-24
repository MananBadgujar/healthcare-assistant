package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.healthcare.assistant.entity.AdherenceLog;
import java.util.Optional;
import java.util.List;

@Repository
public interface AdherenceLogRepository extends JpaRepository<AdherenceLog, Long> {
    List<AdherenceLog> findByPatientId(Long patientId);
    List<AdherenceLog> findByMedicationId(Long medicationId);
    List<AdherenceLog> findByTaken(Boolean taken);
}