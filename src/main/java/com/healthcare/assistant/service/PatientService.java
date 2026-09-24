package com.healthcare.assistant.service;

import java.util.Optional;

import com.healthcare.assistant.entity.Patient;

public interface PatientService {
    Optional<Patient> createPatient(String firstName, String lastName, String dateOfBirth, String gender);
    Optional<Patient> getPatientById(Long id);
    Optional<Patient> updatePatient(Long id, String firstName, String lastName, String dateOfBirth, String gender);
}