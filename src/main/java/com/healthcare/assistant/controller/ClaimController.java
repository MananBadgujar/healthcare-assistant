package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.CreateClaimRequest;
import com.healthcare.assistant.dto.UpdateClaimStatusRequest;
import com.healthcare.assistant.entity.Billing;
import com.healthcare.assistant.entity.Claim;
import com.healthcare.assistant.entity.Insurance;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.enums.ClaimStatus;
import com.healthcare.assistant.repository.BillingRepository;
import com.healthcare.assistant.repository.InsuranceRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.ClaimService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

	private final ClaimService claimService;
	private final PatientRepository patientRepository;
	private final InsuranceRepository insuranceRepository;
	private final BillingRepository billingRepository;

	@Autowired
	public ClaimController(ClaimService claimService, PatientRepository patientRepository,
			InsuranceRepository insuranceRepository, BillingRepository billingRepository) {

		this.claimService = claimService;
		this.patientRepository = patientRepository;
		this.insuranceRepository = insuranceRepository;
		this.billingRepository = billingRepository;
	}

	@PostMapping
	public ResponseEntity<Claim> createClaim(@Valid @RequestBody CreateClaimRequest request) {

		Claim claim = claimService.createClaim(mapToEntity(request));
		return ResponseEntity.ok(claim);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Claim> getClaim(@PathVariable Long id) {

		Claim claim = claimService.getClaim(id);
		return ResponseEntity.ok(claim);
	}

	@GetMapping("/patient/{patientId}")
	public ResponseEntity<List<Claim>> getClaimsByPatient(@PathVariable Long patientId) {

		List<Claim> claims = claimService.getClaimsByPatientId(patientId);
		return ResponseEntity.ok(claims);
	}

	@GetMapping("/insurance/{insuranceId}")
	public ResponseEntity<List<Claim>> getClaimsByInsurance(@PathVariable Long insuranceId) {

		List<Claim> claims = claimService.getClaimsByInsuranceId(insuranceId);
		return ResponseEntity.ok(claims);
	}

	@PostMapping("/{id}/submit")
	public ResponseEntity<Claim> submitClaim(@PathVariable Long id) {

		Claim claim = claimService.submitClaim(id);
		return ResponseEntity.ok(claim);
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Claim> updateClaimStatus(@PathVariable Long id,
			@Valid @RequestBody UpdateClaimStatusRequest request) {

		Claim updated = claimService.updateClaimStatus(id, request.getStatus());

		if (request.getRejectionReason() != null && !request.getRejectionReason().trim().isEmpty()) {

			updated.setRejectionReason(request.getRejectionReason());
			claimService.rejectClaim(id, request.getRejectionReason());
		}

		return ResponseEntity.ok(updated);
	}

	private Claim mapToEntity(CreateClaimRequest req) {

		Claim claim = new Claim();

		Patient patient = patientRepository.findById(req.getPatientId())
				.orElseThrow(() -> new RuntimeException("Patient not found"));

		Insurance insurance = insuranceRepository.findById(req.getInsuranceId())
				.orElseThrow(() -> new RuntimeException("Insurance not found"));

		Billing invoice = billingRepository.findById(req.getInvoiceId())
				.orElseThrow(() -> new RuntimeException("Billing not found"));

		claim.setPatient(patient);
		claim.setInsurance(insurance);
		claim.setInvoice(invoice);

		claim.setClaimNumber(req.getClaimNumber());
		claim.setClaimAmount(req.getClaimAmount());

		ClaimStatus status = req.getClaimStatus() == null ? ClaimStatus.PENDING
				: ClaimStatus.valueOf(req.getClaimStatus().name());

		claim.setClaimStatus(status);

		return claim;
	}
}