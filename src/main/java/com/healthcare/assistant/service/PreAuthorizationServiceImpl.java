package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.PreAuthorization;
import com.healthcare.assistant.entity.enums.PreAuthorizationStatus;
import com.healthcare.assistant.repository.PreAuthorizationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PreAuthorizationServiceImpl implements PreAuthorizationService {

    private final PreAuthorizationRepository preAuthorizationRepository;

    @Autowired
    public PreAuthorizationServiceImpl(PreAuthorizationRepository preAuthorizationRepository) {
        this.preAuthorizationRepository = preAuthorizationRepository;
    }

    @Override
    public PreAuthorization createPreAuthorization(PreAuthorization preAuthorization) {
        return preAuthorizationRepository.save(preAuthorization);
    }

    @Override
    public PreAuthorization getPreAuthorization(Long id) {
        Optional<PreAuthorization> opt = preAuthorizationRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public List<PreAuthorization> getPreAuthorizationsByPatientId(Long patientId) {
        return preAuthorizationRepository.findAll().stream()
                .filter(p -> p.getPatient() != null && p.getPatient().getId().equals(patientId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<PreAuthorization> getPreAuthorizationsByInsuranceId(Long insuranceId) {
        return preAuthorizationRepository.findAll().stream()
                .filter(p -> p.getInsurance() != null && p.getInsurance().getId().equals(insuranceId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PreAuthorization submitPreAuthorization(Long id) {
        PreAuthorization pa = getPreAuthorization(id);
        if (pa != null) {
            pa.setStatus(PreAuthorizationStatus.SUBMITTED);
            return preAuthorizationRepository.save(pa);
        }
        return null;
    }

    @Override
    public PreAuthorization approvePreAuthorization(Long id) {
        PreAuthorization pa = getPreAuthorization(id);
        if (pa != null) {
            pa.setStatus(PreAuthorizationStatus.APPROVED);
            return preAuthorizationRepository.save(pa);
        }
        return null;
    }

    @Override
    public PreAuthorization rejectPreAuthorization(Long id, String reason) {
        PreAuthorization pa = getPreAuthorization(id);
        if (pa != null) {
            pa.setStatus(PreAuthorizationStatus.REJECTED);
            pa.setRejectionReason(reason);
            return preAuthorizationRepository.save(pa);
        }
        return null;
    }

    @Override
    public PreAuthorization updatePreAuthorizationStatus(Long id, String status) {
        PreAuthorization pa = getPreAuthorization(id);
        if (pa != null) {
            try {
                var newStatus = PreAuthorizationStatus.valueOf(status);
                pa.setStatus(newStatus);
                return preAuthorizationRepository.save(pa);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}