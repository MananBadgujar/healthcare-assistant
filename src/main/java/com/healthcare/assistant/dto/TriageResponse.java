package com.healthcare.assistant.dto;

import java.util.List;

public class TriageResponse {
    private String severity;
    private String urgency;
    private List<String> possibleCategories;
    private List<String> redFlags;
    private String recommendedNextStep;
    private String explanation;
    private double confidence;
    private boolean requiresProfessionalEvaluation;

    // Getters and Setters
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public List<String> getPossibleCategories() { return possibleCategories; }
    public void setPossibleCategories(List<String> possibleCategories) { this.possibleCategories = possibleCategories; }
    public List<String> getRedFlags() { return redFlags; }
    public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }
    public String getRecommendedNextStep() { return recommendedNextStep; }
    public void setRecommendedNextStep(String recommendedNextStep) { this.recommendedNextStep = recommendedNextStep; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean isRequiresProfessionalEvaluation() { return requiresProfessionalEvaluation; }
    public void setRequiresProfessionalEvaluation(boolean requiresProfessionalEvaluation) { this.requiresProfessionalEvaluation = requiresProfessionalEvaluation; }
}