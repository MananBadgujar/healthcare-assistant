package com.healthcare.medicationservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medications")
public class Medication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private String name;
    private String dosage;
    private String frequency;
    private String status = "ACTIVE";
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; } public void setPatientId(Long v) { this.patientId = v; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getDosage() { return dosage; } public void setDosage(String v) { this.dosage = v; }
    public String getFrequency() { return frequency; } public void setFrequency(String v) { this.frequency = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
}
