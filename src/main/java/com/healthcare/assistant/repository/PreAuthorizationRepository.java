package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.PreAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreAuthorizationRepository extends JpaRepository<PreAuthorization, Long> {
}