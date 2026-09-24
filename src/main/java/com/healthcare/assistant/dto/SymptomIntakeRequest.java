package com.healthcare.assistant.dto;

import java.util.List;

public class SymptomIntakeRequest {
    private List<String> symptoms;
    private String duration;
    private String severity;
    private Integer age;
    private String gender;
    private List<String> existingConditions;
    private List<String> medications;
    private List<String> allergies;
    private List<String> vitalSigns;
    private String patientContext;

    // Getters and Setters
    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public List<String> getExistingConditions() { return existingConditions; }
    public void setExistingConditions(List<String> existingConditions) { this.existingConditions = existingConditions; }
    public List<String> getMedications() { return medications; }
    public void setMedications(List<String> medications) { this.medications = medications; }
    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }
    public List<String> getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(List<String> vitalSigns) { this.vitalSigns = vitalSigns; }
    public String getPatientContext() { return patientContext; }
    public void setPatientContext(String patientContext) { this.patientContext = patientContext; }
}