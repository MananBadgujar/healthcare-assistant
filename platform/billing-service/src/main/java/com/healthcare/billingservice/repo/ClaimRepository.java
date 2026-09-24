package com.healthcare.billingservice.repo;

import com.healthcare.billingservice.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
