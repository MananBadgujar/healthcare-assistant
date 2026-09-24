package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Claim;
import java.util.List;

public interface ClaimService {
    Claim createClaim(Claim claim);
    Claim getClaim(Long id);
    List<Claim> getClaimsByPatientId(Long patientId);
    List<Claim> getClaimsByInsuranceId(Long insuranceId);
    Claim submitClaim(Long id);
    Claim approveClaim(Long id);
    Claim rejectClaim(Long id, String reason);
    Claim updateClaimStatus(Long id, String status);
}