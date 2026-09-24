package com.healthcare.assistant.service;

import java.util.List;
import java.util.Optional;
import com.healthcare.assistant.dto.MedicationRequest;
import com.healthcare.assistant.dto.AdherenceLogRequest;
import com.healthcare.assistant.dto.RefillRequest;
import com.healthcare.assistant.entity.Medication;

public interface MedicationService {
    Medication createMedication(String name, String dosage, String frequency, String instructions, Long patientId);
    Optional<Medication> getMedicationById(Long id);
    List<Medication> getMedicationsByPatientId(Long patientId);
    Optional<Medication> updateMedication(Long id, String name, String dosage, String frequency, String instructions);
    void deleteMedication(Long id);

    // Overloaded versions that enforce patient ownership
    Optional<Medication> updateMedication(Long id, String name, String dosage, String frequency, String instructions, Long patientId);
    void deleteMedication(Long id, Long patientId);

    // DTO-based overloads for Phase 5 APIs
    String createMedication(MedicationRequest request);
    String logAdherence(AdherenceLogRequest request);
    int calculateAdherenceScore(Long patientId);
    String requestRefill(RefillRequest request);
}