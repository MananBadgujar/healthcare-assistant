package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.assistant.entity.Encounter;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {
}
