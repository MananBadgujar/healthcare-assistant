package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Clinic;

import java.util.List;
import java.util.Optional;

public interface ClinicService {
    Clinic createClinic(Clinic clinic);
    Optional<Clinic> getClinicById(Long id);
    List<Clinic> getAllClinics();
    Clinic updateClinic(Long id, Clinic clinic);
    void deleteClinic(Long id);
    Optional<Clinic> findByName(String name);
}