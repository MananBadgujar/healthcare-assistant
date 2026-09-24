package com.healthcare.assistant.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MedicationRequest {
    private Long patientId;
    @NotBlank(message = "Medication name must not be blank")
    private String medicationName;
    @NotBlank(message = "Dosage must not be blank")
    @Pattern(regexp = "\\d+\\s*(mg|ml|tablet|capsule|dose)\\b", message = "Dosage format must be numeric with unit")
    private String dosage;
    @NotBlank(message = "Frequency must not be blank")
    @Pattern(regexp = "\\d+\\s*(times|daily|hourly)\\b", message = "Frequency format must be numeric with schedule")
    private String frequency;
    private String route;
    @NotBlank(message = "Instructions must not be blank")
    private String instructions;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long prescriberId;

    // Getters and Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Long getPrescriberId() { return prescriberId; }
    public void setPrescriberId(Long prescriberId) { this.prescriberId = prescriberId; }
}