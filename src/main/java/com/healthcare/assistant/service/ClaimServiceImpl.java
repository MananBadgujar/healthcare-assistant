package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Claim;
import com.healthcare.assistant.entity.enums.ClaimStatus;
import com.healthcare.assistant.repository.ClaimRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;

    @Autowired
    public ClaimServiceImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public Claim createClaim(Claim claim) {
        return claimRepository.save(claim);
    }

    @Override
    public Claim getClaim(Long id) {
        Optional<Claim> opt = claimRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<Claim> getClaimsByPatientId(Long patientId) {
        return claimRepository.findAll().stream()
                .filter(c -> c.getPatient() != null && c.getPatient().getId().equals(patientId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Claim> getClaimsByInsuranceId(Long insuranceId) {
        return claimRepository.findAll().stream()
                .filter(c -> c.getInsurance() != null && c.getInsurance().getId().equals(insuranceId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Claim submitClaim(Long id) {
        Claim claim = getClaim(id);
        if (claim != null) {
            claim.setClaimStatus(ClaimStatus.SUBMITTED);
            return claimRepository.save(claim);
        }
        return null;
    }

    @Override
    public Claim approveClaim(Long id) {
        Claim claim = getClaim(id);
        if (claim != null) {
            claim.setClaimStatus(ClaimStatus.APPROVED);
            return claimRepository.save(claim);
        }
        return null;
    }

    @Override
    public Claim rejectClaim(Long id, String reason) {
        Claim claim = getClaim(id);
        if (claim != null) {
            claim.setClaimStatus(ClaimStatus.REJECTED);
            claim.setRejectionReason(reason);
            return claimRepository.save(claim);
        }
        return null;
    }

    @Override
    public Claim updateClaimStatus(Long id, String status) {
        Claim claim = getClaim(id);
        if (claim != null) {
            try {
                var newStatus = ClaimStatus.valueOf(status);
                claim.setClaimStatus(newStatus);
                return claimRepository.save(claim);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}