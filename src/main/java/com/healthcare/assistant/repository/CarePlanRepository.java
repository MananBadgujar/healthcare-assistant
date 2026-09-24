package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.CarePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

public interface CarePlanRepository extends JpaRepository<CarePlan, Long> {
    Optional<CarePlan> findByPatientId(Long patientId);
}