package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findByPatientId(Long patientId);
}