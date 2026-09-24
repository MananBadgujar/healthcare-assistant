package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;

    @Autowired
    public PatientServiceImpl(PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Patient> createPatient(String firstName, String lastName, String dateOfBirth, String gender) {
        Patient patient = new Patient(firstName, lastName, dateOfBirth, gender);
        Patient saved = repository.save(patient);
        return Optional.of(saved);
    }

    @Override
    public Optional<Patient> getPatientById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Patient> updatePatient(Long id, String firstName, String lastName, String dateOfBirth, String gender) {
        Optional<Patient> existing = repository.findById(id);
        if (existing.isPresent()) {
            Patient patient = existing.get();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            repository.save(patient);
            return Optional.of(patient);
        }
        return Optional.empty();
    }
}