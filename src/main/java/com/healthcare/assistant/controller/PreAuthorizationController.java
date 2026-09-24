package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.CreatePreAuthorizationRequest;
import com.healthcare.assistant.dto.UpdatePreAuthorizationStatusRequest;
import com.healthcare.assistant.entity.Insurance;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.PreAuthorization;
import com.healthcare.assistant.entity.enums.PreAuthorizationStatus;
import com.healthcare.assistant.repository.InsuranceRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.PreAuthorizationService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pre-authorizations")
public class PreAuthorizationController {

    private final PreAuthorizationService preAuthorizationService;
    private final PatientRepository patientRepository;
    private final InsuranceRepository insuranceRepository;

    @Autowired
    public PreAuthorizationController(
            PreAuthorizationService preAuthorizationService,
            PatientRepository patientRepository,
            InsuranceRepository insuranceRepository) {

        this.preAuthorizationService = preAuthorizationService;
        this.patientRepository = patientRepository;
        this.insuranceRepository = insuranceRepository;
    }

    @PostMapping
    public ResponseEntity<PreAuthorization> createPreAuthorization(
            @Valid @RequestBody CreatePreAuthorizationRequest request) {

        PreAuthorization pa =
                preAuthorizationService.createPreAuthorization(
                        mapToEntity(request));

        return ResponseEntity.ok(pa);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PreAuthorization> getPreAuthorization(
            @PathVariable Long id) {

        PreAuthorization pa =
                preAuthorizationService.getPreAuthorization(id);

        return ResponseEntity.ok(pa);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PreAuthorization>> getPreAuthorizationsByPatient(
            @PathVariable Long patientId) {

        List<PreAuthorization> list =
                preAuthorizationService
                        .getPreAuthorizationsByPatientId(patientId);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/insurance/{insuranceId}")
    public ResponseEntity<List<PreAuthorization>> getPreAuthorizationsByInsurance(
            @PathVariable Long insuranceId) {

        List<PreAuthorization> list =
                preAuthorizationService
                        .getPreAuthorizationsByInsuranceId(insuranceId);

        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<PreAuthorization> submitPreAuthorization(
            @PathVariable Long id) {

        PreAuthorization pa =
                preAuthorizationService.submitPreAuthorization(id);

        return ResponseEntity.ok(pa);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PreAuthorization> updatePreAuthorizationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePreAuthorizationStatusRequest request) {

        PreAuthorization updated =
                preAuthorizationService.updatePreAuthorizationStatus(
                        id,
                        request.getStatus());

        if (request.getRejectionReason() != null
                && !request.getRejectionReason().trim().isEmpty()) {

            updated.setRejectionReason(request.getRejectionReason());

            preAuthorizationService.rejectPreAuthorization(
                    id,
                    request.getRejectionReason());
        }

        return ResponseEntity.ok(updated);
    }

    private PreAuthorization mapToEntity(
            CreatePreAuthorizationRequest req) {

        PreAuthorization pa = new PreAuthorization();

        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() ->
                        new RuntimeException("Patient not found"));

        Insurance insurance = insuranceRepository.findById(req.getInsuranceId())
                .orElseThrow(() ->
                        new RuntimeException("Insurance not found"));

        pa.setPatient(patient);
        pa.setInsurance(insurance);

        pa.setMedicalInformation(req.getMedicalInformation());
        pa.setServiceInformation(req.getServiceInformation());

        PreAuthorizationStatus status =
                req.getStatus() == null
                        ? PreAuthorizationStatus.PENDING
                        : PreAuthorizationStatus.valueOf(
                                req.getStatus()
                                        .trim()
                                        .toUpperCase());

        pa.setStatus(status);
        pa.setRequestedAmount(req.getRequestedAmount());

        return pa;
    }
}