package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Insurance;
import com.healthcare.assistant.entity.enums.InsuranceStatus;
import com.healthcare.assistant.repository.InsuranceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;

    @Autowired
    public InsuranceServiceImpl(InsuranceRepository insuranceRepository) {
        this.insuranceRepository = insuranceRepository;
    }

    @Override
    public Insurance createInsurance(Insurance insurance) {
        return insuranceRepository.save(insurance);
    }

    @Override
    public Insurance getInsurance(Long id) {
        Optional<Insurance> opt = insuranceRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<Insurance> getInsurancesByPatientId(Long patientId) {
        // Simple filter in memory; repository does not have method; could use query method later.
        return insuranceRepository.findAll().stream()
                .filter(i -> i.getPatient() != null && i.getPatient().getId().equals(patientId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Insurance updateInsuranceStatus(Long id, String status) {
        Insurance insurance = getInsurance(id);
        if (insurance != null) {
            try {
                var newStatus = InsuranceStatus.valueOf(status.toUpperCase());
                insurance.setStatus(newStatus.name());
                return insuranceRepository.save(insurance);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}