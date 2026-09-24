package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.SymptomExtraction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SymptomExtractionServiceImpl implements com.healthcare.assistant.service.SymptomExtractionService {

    // Simple regex patterns for common symptom vocabularies
    private static final Pattern SYMPTOM_PATTERN =
            Pattern.compile("\\b(headache|fever|cough|sore\\s+throat|pain|chest\\s+pain|shortness\\s+of\\s+breath|nausea|vomiting|diarrhea|fatigue|dizziness)\\b", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("\\b(over\\s+the\\s+past|for|during|last)\\s+(\\d+)\\s*(day|days|hour|hours|minute|minutes|second|seconds)\\b", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern SEVERITY_PATTERN =
            Pattern.compile("\\b(mild|moderate|severe)\\b", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern MEDICATION_PATTERN =
            Pattern.compile("\\b(taken|using|on)\\s+(aspirin|ibuprofen|paracetamol|acetaminophen|medication|drugs?)\\b", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern ALLERGY_PATTERN =
            Pattern.compile("\\b(allergic|allergy|react|hives|rash)\\s+to\\s+(\\w+)\\b", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern CONDITION_PATTERN =
            Pattern.compile("\\b(condition|issue|problem)\\s+is\\s+([a-zA-Z\\s]+)\\b", Pattern.UNICODE_CHARACTER_CLASS);

    @Override
    public SymptomExtraction extractSymptoms(String userMessage) {
        SymptomExtraction extraction = new SymptomExtraction(
                userMessage,
                new ArrayList<String>(),
                "",
                "",
                "",
                "",
                "",
                ""
        );

        // Extract symptoms
        Matcher symMatcher = SYMPTOM_PATTERN.matcher(userMessage.toLowerCase());
        Set<String> foundSymptoms = new HashSet<>();
        while (symMatcher.find()) {
            String symptom = symMatcher.group().replaceAll("\\s+", " ");
            switch (symptom) {
                case "headache": foundSymptoms.add("headache"); break;
                case "sore throat": foundSymptoms.add("sore throat"); break;
                case "shortness of breath": foundSymptoms.add("shortness of breath"); break;
                case "breathlessness": foundSymptoms.add("shortness of breath"); break;
                case "stomach ache": foundSymptoms.add("abdominal pain"); break;
                case "abdominal pain": foundSymptoms.add("abdominal pain"); break;
                default: foundSymptoms.add(symptom);
            }
        }
        extraction.getSymptoms().addAll(foundSymptoms);

        // Extract duration
        Matcher durMatcher = DURATION_PATTERN.matcher(userMessage.toLowerCase());
        if (durMatcher.find()) {
            extraction.setDuration(durMatcher.group(2) + " " + durMatcher.group(3));
        }

        // Extract severity
        Matcher sevMatcher = SEVERITY_PATTERN.matcher(userMessage.toLowerCase());
        if (sevMatcher.find()) {
            extraction.setSeverity(sevMatcher.group(1).toLowerCase());
        }

        // Extract medication
        Matcher medMatcher = MEDICATION_PATTERN.matcher(userMessage.toLowerCase());
        if (medMatcher.find()) {
            extraction.setMedication(medMatcher.group(2));
        }

        // Extract allergy
        Matcher allergyMatcher = ALLERGY_PATTERN.matcher(userMessage.toLowerCase());
        if (allergyMatcher.find()) {
            extraction.setAllergy(allergyMatcher.group(2));
        }

        // Extract medical condition
        Matcher condMatcher = CONDITION_PATTERN.matcher(userMessage.toLowerCase());
        if (condMatcher.find()) {
            extraction.setMedicalCondition(condMatcher.group(2).trim());
        }

        // Normalization: map colloquial terms to standardized names (currently identity)
        Set<String> uniqueSymptoms = new HashSet<>(extraction.getSymptoms());
        extraction.getSymptoms().clear();
        extraction.getSymptoms().addAll(uniqueSymptoms);

        return extraction;
    }
}