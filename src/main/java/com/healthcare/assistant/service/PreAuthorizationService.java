package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.PreAuthorization;
import java.util.List;

public interface PreAuthorizationService {
    PreAuthorization createPreAuthorization(PreAuthorization preAuthorization);
    PreAuthorization getPreAuthorization(Long id);
    List<PreAuthorization> getPreAuthorizationsByPatientId(Long patientId);
    List<PreAuthorization> getPreAuthorizationsByInsuranceId(Long insuranceId);
    PreAuthorization submitPreAuthorization(Long id);
    PreAuthorization approvePreAuthorization(Long id);
    PreAuthorization rejectPreAuthorization(Long id, String reason);
    PreAuthorization updatePreAuthorizationStatus(Long id, String status);
}