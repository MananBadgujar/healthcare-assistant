package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Consent;
import com.healthcare.assistant.repository.ConsentRepository;
import com.healthcare.assistant.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository repository;

    @Autowired
    public ConsentServiceImpl(ConsentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Consent> getPatientConsents(Long patientId) {
        return repository.findByPatientId(patientId);
    }

    @Override
    public Consent createConsent(Long patientId, String type, Boolean active) {
        Consent consent = new Consent(patientId, type, active);
        return repository.save(consent);
    }

    @Override
    public void revokeConsent(Long consentId) {
        repository.deleteById(consentId);
    }
}