package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.MedicationRequest;
import com.healthcare.assistant.dto.AdherenceLogRequest;
import com.healthcare.assistant.dto.RefillRequest;
import com.healthcare.assistant.entity.AdherenceLog;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Prescription;
import com.healthcare.assistant.repository.AdherenceLogRepository;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.repository.PrescriptionRepository;
import com.healthcare.assistant.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final AdherenceLogRepository adherenceLogRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public MedicationServiceImpl(MedicationRepository medicationRepository, PatientRepository patientRepository,
                                 AdherenceLogRepository adherenceLogRepository, PrescriptionRepository prescriptionRepository) {
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
        this.adherenceLogRepository = adherenceLogRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    private boolean isValidDosage(String dosage) {
        if (!StringUtils.hasText(dosage)) {
            return false;
        }
        return Pattern.compile("\\d+\\s*(mg|ml|tablet|capsule|dose)\\b").matcher(dosage).matches();
    }

    private boolean isValidFrequency(String frequency) {
        if (!StringUtils.hasText(frequency)) {
            return false;
        }
        return Pattern.compile("\\d+\\s*(times|daily|hourly)\\b").matcher(frequency).matches();
    }

    private void validatePatientExists(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID must not be null");
        }
        if (!patientRepository.existsById(patientId)) {
            throw new IllegalArgumentException("Patient not found with ID: " + patientId);
        }
    }

    @Override
    public Medication createMedication(String name, String dosage, String frequency, String instructions, Long patientId) {
        // Validate required fields
        if (!StringUtils.hasText(name) || !StringUtils.hasText(dosage) || !StringUtils.hasText(frequency) || !StringUtils.hasText(instructions)) {
            throw new IllegalArgumentException("Name, dosage, frequency, and instructions must not be blank");
        }
        // Validate dosage and frequency format
        if (!isValidDosage(dosage)) {
            throw new IllegalArgumentException("Invalid dosage format");
        }
        if (!isValidFrequency(frequency)) {
            throw new IllegalArgumentException("Invalid frequency format");
        }
        // Validate patient exists
        validatePatientExists(patientId);

        // Check for duplicate medication for the same patient (case‑insensitive name and dosage/frequency match)
        List<Medication> existingMedications = medicationRepository.findByPatientId(patientId);
        for (Medication existing : existingMedications) {
            if (existing.getName().equalsIgnoreCase(name.trim())
                    && existing.getDosage().equalsIgnoreCase(dosage.trim())
                    && existing.getFrequency().equalsIgnoreCase(frequency.trim())) {
                throw new IllegalArgumentException("Duplicate medication for patient");
            }
        }

        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        Patient patient = patientOpt.orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Medication medication = new Medication(name, dosage, frequency, instructions, patient);
        return medicationRepository.save(medication);
    }

    @Override
    public Optional<Medication> getMedicationById(Long id) {
        return medicationRepository.findById(id);
    }

    @Override
    public List<Medication> getMedicationsByPatientId(Long patientId) {
        return medicationRepository.findByPatientId(patientId);
    }

    @Override
    public Optional<Medication> updateMedication(Long id, String name, String dosage, String frequency, String instructions) {
        Optional<Medication> existing = medicationRepository.findById(id);
        if (existing.isPresent()) {
            Medication medication = existing.get();
            // Validate fields if they are being updated (if null or blank, we keep the existing?
            // But the controller's @Valid ensures they are not blank in the request, so we can assume they are valid.
            // However, to be safe, we validate if they are provided.
            if (StringUtils.hasText(name)) {
                medication.setName(name);
            }
            if (StringUtils.hasText(dosage)) {
                if (!isValidDosage(dosage)) {
                    throw new IllegalArgumentException("Invalid dosage format");
                }
                medication.setDosage(dosage);
            }
            if (StringUtils.hasText(frequency)) {
                if (!isValidFrequency(frequency)) {
                    throw new IllegalArgumentException("Invalid frequency format");
                }
                medication.setFrequency(frequency);
            }
            if (StringUtils.hasText(instructions)) {
                medication.setInstructions(instructions);
            }
            return Optional.of(medicationRepository.save(medication));
        }
        return Optional.empty();
    }

    @Override
    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }

    // Ownership‑enforced overloads
    @Override
    public Optional<Medication> updateMedication(Long id, String name, String dosage, String frequency, String instructions, Long patientId) {
        Optional<Medication> existing = medicationRepository.findById(id);
        if (existing.isPresent()) {
            Medication medication = existing.get();
            if (!medication.getPatient().getId().equals(patientId)) {
                // enforce ownership – do not update if not the owner
                return Optional.empty();
            }
            // Validate fields similarly to the other update method
            if (StringUtils.hasText(name)) {
                medication.setName(name);
            }
            if (StringUtils.hasText(dosage)) {
                if (!isValidDosage(dosage)) {
                    throw new IllegalArgumentException("Invalid dosage format");
                }
                medication.setDosage(dosage);
            }
            if (StringUtils.hasText(frequency)) {
                if (!isValidFrequency(frequency)) {
                    throw new IllegalArgumentException("Invalid frequency format");
                }
                medication.setFrequency(frequency);
            }
            if (StringUtils.hasText(instructions)) {
                medication.setInstructions(instructions);
            }
            return Optional.of(medicationRepository.save(medication));
        }
        return Optional.empty();
    }

    @Override
    public void deleteMedication(Long id, Long patientId) {
        // Find by id and ensure it belongs to the patient before deletion
        medicationRepository.findById(id).ifPresent(medication -> {
            if (!medication.getPatient().getId().equals(patientId)) {
                // enforce ownership – do not delete if not the owner
                throw new SecurityException("Medication does not belong to the patient");
            }
            medicationRepository.deleteById(id);
        });
    }

    // New DTO-based Phase 5 APIs
    @Override
    public String createMedication(MedicationRequest request) {
        Medication medication = createMedication(
                request.getMedicationName(),
                request.getDosage(),
                request.getFrequency(),
                request.getInstructions(),
                request.getPatientId()
        );
        return medication.getId().toString();
    }

    @Override
    public String logAdherence(AdherenceLogRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId()).orElse(null);
        Medication medication = medicationRepository.findById(request.getMedicationId()).orElse(null);
        if (patient == null || medication == null) {
            return "not found";
        }
        AdherenceLog log = new AdherenceLog(patient, medication, LocalDateTime.now(), request.getTaken(), request.getNotes());
        adherenceLogRepository.save(log);
        return "logged";
    }

    @Override
    public int calculateAdherenceScore(Long patientId) {
        List<AdherenceLog> logs = adherenceLogRepository.findByPatientId(patientId);
        if (logs.isEmpty()) {
            return 0;
        }
        long takenCount = logs.stream().filter(l -> Boolean.TRUE.equals(l.getTaken())).count();
        return (int) (takenCount * 100L / logs.size());
    }

    @Override
    public String requestRefill(RefillRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId()).orElse(null);
        Medication medication = medicationRepository.findById(request.getMedicationId()).orElse(null);
        if (patient == null || medication == null) {
            return "not found";
        }
        Prescription prescription = new Prescription(patient, medication, "refill dosage", "refill frequency", request.getReason(),
                LocalDate.now(), LocalDate.now().plusDays(30));
        prescriptionRepository.save(prescription);
        return prescription.getId().toString();
    }
}