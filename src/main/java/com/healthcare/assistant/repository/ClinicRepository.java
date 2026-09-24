package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    Optional<Clinic> findByName(String name);
}