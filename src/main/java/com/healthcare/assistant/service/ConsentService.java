package com.healthcare.assistant.service;

import java.util.List;
import java.util.Optional;

import com.healthcare.assistant.entity.Consent;

public interface ConsentService {
    Optional<Consent> getPatientConsents(Long patientId);
    Consent createConsent(Long patientId, String type, Boolean active);
    void revokeConsent(Long consentId);
}