package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.healthcare.assistant.entity.Medication;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByPatientId(Long patientId);
    Optional<Medication> findByName(String name);
    Optional<Medication> findByIdAndPatientId(Long id, Long patientId);
}