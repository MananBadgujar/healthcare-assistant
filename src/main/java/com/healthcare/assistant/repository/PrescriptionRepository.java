package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.healthcare.assistant.entity.Prescription;
import java.util.Optional;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByMedicationId(Long medicationId);
    List<Prescription> findByStatus(String status);
}