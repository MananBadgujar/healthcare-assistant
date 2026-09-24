package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of {@link PatientContextService} that extracts the
 * currently authenticated patient and builds a sanitized context DTO.
 */
@Service
public class PatientContextServiceImpl implements com.healthcare.assistant.service.PatientContextService {

    private final PatientRepository repository;

    public PatientContextServiceImpl(PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public PatientContext getCurrentPatientContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated request");
        }
        String email = authentication.getName();
        Optional<Patient> patientOptional = repository.findByEmail(email);
        if (patientOptional.isEmpty()) {
            throw new IllegalStateException("Patient not found for email: " + email);
        }
        Patient patient = patientOptional.get();
        return new PatientContext(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getGender(),
                patient.getDateOfBirth()
        );
    }
}