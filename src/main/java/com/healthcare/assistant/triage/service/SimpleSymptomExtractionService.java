package com.healthcare.assistant.triage.service;

import org.springframework.stereotype.Service;

import com.healthcare.assistant.triage.TriageRequest;
import com.healthcare.assistant.triage.result.SymptomExtractionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SimpleSymptomExtractionService implements SymptomExtractionService {

    private static final List<String> KNOWN_SYMPTOMS = List.of(
            "shortness of breath",
            "difficulty breathing",
            "breathing difficulty",
            "chest pain",
            "sore throat",
            "abdominal pain",
            "stomach pain",
            "loss of consciousness",
            "loss of appetite",
            "headache",
            "fever",
            "cough",
            "nausea",
            "vomiting",
            "diarrhea",
            "body pain",
            "pain",
            "fatigue",
            "dizziness",
            "rash",
            "stroke",
            "collapse",
            "unconscious",
            "heart attack"
    );

    private static final Set<String> EMERGENCY_SYMPTOMS = Set.of(
            "chest pain",
            "shortness of breath",
            "difficulty breathing",
            "breathing difficulty",
            "loss of consciousness",
            "unconscious",
            "collapse",
            "stroke",
            "heart attack",
            "persistent vomiting",
            "vomiting blood",
            "blood in stool",
            "severe abdominal pain",
            "sudden swelling",
            "sudden vision loss",
            "severe headache",
            "weakness",
            "numbness",
            "uncontrolled bleeding"
    );

    @Override
    public SymptomExtractionResult extractSymptoms(TriageRequest request) {

        SymptomExtractionResult result = new SymptomExtractionResult();

        if (request == null) {
            result.setExtractedSymptoms(Collections.emptyList());
            result.setSeverity("NONE");
            result.setIdentifiedCondition("NONE");
            result.setMedicationSuggestion("");
            result.setClinicalGuidance("");
            result.setRedFlag(false);
            result.setEmergency(false);
            return result;
        }

        /*
         * Keep explicitly supplied symptoms for backward compatibility.
         * LinkedHashSet removes duplicates while preserving insertion order.
         */
        Set<String> mergedSymptoms = new LinkedHashSet<>();

        if (request.getSymptoms() != null) {
            for (String symptom : request.getSymptoms()) {
                if (symptom != null && !symptom.isBlank()) {
                    mergedSymptoms.add(normalize(symptom));
                }
            }
        }

        /*
         * Extract symptoms from free-form question.
         */
        String question = request.getQuestion();

        if (question != null && !question.isBlank()) {

            String normalizedQuestion = normalize(question);

            for (String symptom : KNOWN_SYMPTOMS) {
                String normalizedSymptom = normalize(symptom);

                if (containsPhrase(normalizedQuestion, normalizedSymptom)) {
                    mergedSymptoms.add(normalizedSymptom);
                }
            }
        }

        List<String> symptoms = new ArrayList<>(mergedSymptoms);
        result.setExtractedSymptoms(symptoms);

        /*
         * Severity
         */
        String severity = request.getSeverity();

        if (severity == null || severity.isBlank()) {
            severity = "NONE";
        }

        result.setSeverity(severity);

        /*
         * Default structured fields
         */
        result.setIdentifiedCondition("NONE");
        result.setMedicationSuggestion("");
        result.setClinicalGuidance("");

        /*
         * Red-flag detection must use the FINAL merged symptom list.
         */
        boolean hasEmergencySymptom = symptoms.stream()
                .map(SimpleSymptomExtractionService::normalize)
                .anyMatch(EMERGENCY_SYMPTOMS::contains);

        boolean severe = "severe".equalsIgnoreCase(result.getSeverity());

        if (severe && hasEmergencySymptom) {
            result.setRedFlag(true);
            result.setEmergency(true);
        } else {
            result.setRedFlag(false);
            result.setEmergency(false);
        }

        return result;
    }

    private static String normalize(String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean containsPhrase(String text, String phrase) {
        if (text == null || text.isBlank() || phrase == null || phrase.isBlank()) {
            return false;
        }

        String[] words = text.split("\\s+");
        String[] phraseWords = phrase.split("\\s+");

        if (phraseWords.length > words.length) {
            return false;
        }

        for (int i = 0; i <= words.length - phraseWords.length; i++) {

            boolean match = true;

            for (int j = 0; j < phraseWords.length; j++) {
                if (!words[i + j].equals(phraseWords[j])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return true;
            }
        }

        return false;
    }
}