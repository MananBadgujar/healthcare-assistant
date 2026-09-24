package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.healthcare.assistant.entity.Refill;
import java.util.Optional;
import java.util.List;

@Repository
public interface RefillRepository extends JpaRepository<Refill, Long> {
    List<Refill> findByPrescriptionId(Long prescriptionId);
    List<Refill> findByStatus(String status);
}