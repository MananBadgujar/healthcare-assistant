package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.service.HealthcareScopeService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of HealthcareScopeService that uses keyword-based detection
 * to determine if a question is healthcare-related.
 */
@Service
public class HealthcareScopeServiceImpl implements HealthcareScopeService {

    // Keywords that indicate healthcare-related questions
    private static final List<String> HEALTHCARE_KEYWORDS = Arrays.asList(
        // Symptoms and conditions
        "pain", "ache", "hurt", "sore", "fever", "temperature", "headache", "migraine",
        "nausea", "vomit", "diarrhea", "constipation", "cough", "sneeze", "congestion",
        "rash", "itch", "swelling", "inflammation", "infection", "bacteria", "virus",
        "flu", "cold", "allergy", "asthma", "diabetes", "hypertension", "blood pressure",
        "heart", "chest", "shortness of breath", "dizzy", "dizziness", "faint", "fatigue",
        "tired", "weak", "weakness", "numb", "tingling", "paralysis", "seizure", "convulsion",
        "bleeding", "blood", "bruise", "wound", "cut", "burn", "fracture", "break", "sprain",
        "strain", "arthritis", "joint", "muscle", "back pain", "neck pain", "stomach",
        "abdominal", "belly", "gut", "digestive", "constipation", "diarrhea", "gas",
        "bloating", "heartburn", "indigestion", "reflux", "ulcer", "constipation",
        "anxiety", "depression", "stress", "mental health", "therapy", "counseling",
        "sleep", "insomnia", "tired", "exhausted", "energy", "appetite", "weight",
        "pregnancy", "prenatal", "postpartum", "menstrual", "period", "menopause",
        "erectile", "libido", "sexual health", "std", "sti", "infection",
        "medication", "medicine", "drug", "prescription", "dosage", "pill", "tablet",
        "capsule", "injection", "vaccine", "vaccination", "immunization",
        "doctor", "physician", "clinician", "nurse", "hospital", "clinic", "emergency",
        "urgent care", "appointment", "checkup", "physical", "exam", "test", "screening",
        "lab", "blood test", "x-ray", "mri", "ct scan", "ultrasound", "biopsy",
        "surgery", "operation", "procedure", "treatment", "therapy", "rehabilitation",
        "physical therapy", "occupational therapy", "speech therapy",
        "wellness", "prevention", "healthy", "health", "medical", "healthcare",
        "symptom", "condition", "disease", "disorder", "illness", " sickness",
        "remedy", "cure", "treatment", "first aid", "emergency", "911", "ambulance",
        "vital signs", "heart rate", "pulse", "breathing", "respiration", "oxygen",
        "temperature", "thermometer", "blood sugar", "glucose", "cholesterol"
    );

    @Override
    public boolean isHealthcareRelated(String question) {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }

        String lowerQuestion = question.toLowerCase().trim();

        // Check if any healthcare keyword appears in the question
        for (String keyword : HEALTHCARE_KEYWORDS) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        // Additional pattern matching for common healthcare phrases
        String[] healthcarePatterns = {
            "\\b(what is|what are|how to|how do|why do|when should|should i)\\b.*\\b(symptom|pain|ache|hurt|fever|temperature|medication|medicine|drug|doctor|hospital|clinic)\\b",
            "\\b(i feel|i am|i have|i got|i\\'m)\\b.*\\b(sick|ill|unwell|bad|poor|terrible|awful)\\b",
            "\\b(my|me)\\b.*\\b(hurts|aches|pains|feels|feeling)\\b",
            "\\b(should i|do i need|when to|how long)\\b.*\\b(see|visit|go to)\\b.*\\b(doctor|physician|hospital|clinic|emergency)\\b"
        };

        for (String pattern : healthcarePatterns) {
            if (lowerQuestion.matches(pattern)) {
                return true;
            }
        }

        return false;
    }
}