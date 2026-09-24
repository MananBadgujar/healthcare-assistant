package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Clinic;
import com.healthcare.assistant.repository.ClinicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;

    @Autowired
    public ClinicServiceImpl(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Override
    public Clinic createClinic(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    @Override
    public Optional<Clinic> getClinicById(Long id) {
        return clinicRepository.findById(id);
    }

    @Override
    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    @Override
    @Transactional
    public Clinic updateClinic(Long id, Clinic clinic) {
        Clinic existing = clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));
        existing.setName(clinic.getName());
        existing.setAddress(clinic.getAddress());
        existing.setPhone(clinic.getPhone());
        existing.setEmail(clinic.getEmail());
        existing.setDescription(clinic.getDescription());
        return clinicRepository.save(existing);
    }

    @Override
    public void deleteClinic(Long id) {
        clinicRepository.deleteById(id);
    }

    @Override
    public Optional<Clinic> findByName(String name) {
        return clinicRepository.findByName(name);
    }
}