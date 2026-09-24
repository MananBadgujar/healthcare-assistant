package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.CreateInsuranceRequest;
import com.healthcare.assistant.dto.UpdateInsuranceStatusRequest;
import com.healthcare.assistant.entity.Insurance;
import com.healthcare.assistant.service.InsuranceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/insurance")
public class InsuranceController {

    private final InsuranceService insuranceService;

    @Autowired
    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @PostMapping
    public ResponseEntity<Insurance> createInsurance(@Valid @RequestBody CreateInsuranceRequest request) {
        Insurance insurance = insuranceService.createInsurance(mapToEntity(request));
        return ResponseEntity.ok(insurance);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insurance> getInsurance(@PathVariable Long id) {
        Insurance insurance = insuranceService.getInsurance(id);
        return ResponseEntity.ok(insurance);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Insurance>> getInsurancesByPatient(@PathVariable Long patientId) {
        List<Insurance> insurances = insuranceService.getInsurancesByPatientId(patientId);
        return ResponseEntity.ok(insurances);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Insurance> updateInsuranceStatus(@PathVariable Long id, @Valid @RequestBody UpdateInsuranceStatusRequest request) {
        Insurance updated = insuranceService.updateInsuranceStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    // Simple mapping from request to entity (minimal)
    private Insurance mapToEntity(CreateInsuranceRequest req) {
        Insurance i = new Insurance();
        i.setProviderName(req.getProviderName());
        i.setPolicyNumber(req.getPolicyNumber());
        i.setMemberId(req.getMemberId());
        i.setPlanName(req.getPlanName());
        // other fields left null
        return i;
    }
}