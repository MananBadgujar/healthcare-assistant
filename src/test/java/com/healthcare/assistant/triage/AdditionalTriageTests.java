package com.healthcare.assistant.triage;

import com.healthcare.assistant.triage.TriageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdditionalTriageTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // No specific setup required for these unit tests
    }

    @Test
    @DisplayName("Normal symptom query should return structured result")
    void normalSymptomQuery_shouldReturnStructuredResult() throws Exception {
        TriageRequest request = new TriageRequest();
        request.setQuestion("I have been coughing for 3 days.");
        request.setAge(28);
        request.setGender("female");
        request.setSymptoms(java.util.List.of("cough", "fever"));
        request.setSeverity("moderate");
        request.setDuration("3 days");
        request.setContext("home");

        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.extractedSymptoms", containsInAnyOrder("cough", "fever")))
                .andExpect(jsonPath("$.redFlag").value(false))
                .andExpect(jsonPath("$.emergency").value(false));
    }

    @Test
    @DisplayName("Non‑healthcare query should be redirected to healthcare scope")
    void nonHealthcareQuery_shouldBeRedirected() throws Exception {
        TriageRequest request = new TriageRequest();
        request.setQuestion("What are the best vacation spots in Europe?");

        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clinicalGuidance").value("I'm a healthcare assistant and can only help with healthcare-related questions. Please ask about symptoms, medical conditions, treatments, or other health concerns."));
    }

    @Test
    @DisplayName("Emergency red‑flag query should set redFlag and emergency flags")
    void emergencyRedFlag_shouldSetFlags() throws Exception {
        TriageRequest request = new TriageRequest();
        request.setQuestion("I experience sudden chest pain and shortness of breath.");
        request.setAge(65);
        request.setGender("male");
        request.setSymptoms(java.util.List.of("chest pain", "shortness of breath"));
        request.setSeverity("severe");
        request.setDuration("minutes");
        request.setContext("at rest");

        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.redFlag").value(true))
                .andExpect(jsonPath("$.emergency").value(true));
    }

    @Test
    @DisplayName("Malformed request should return Bad Request")
    void malformedRequest_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Multi‑turn conversation context is preserved (basic smoke test)")
    void multiTurnConversation_basicSmokeTest() throws Exception {
        // First turn – simple symptom query
        TriageRequest firstRequest = new TriageRequest();
        firstRequest.setQuestion("I have a mild headache.");
        firstRequest.setAge(35);
        firstRequest.setSymptoms(java.util.List.of("headache"));
        firstRequest.setSeverity("mild");
        firstRequest.setDuration("1 hour");
        firstRequest.setContext("working");

        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Second turn – follow‑up with related question
        TriageRequest secondRequest = new TriageRequest();
        secondRequest.setQuestion("Should I take medication for it?");
        secondRequest.setAge(35);
        secondRequest.setSymptoms(java.util.List.of("headache"));
        secondRequest.setSeverity("mild");
        secondRequest.setDuration("1 hour");
        secondRequest.setContext("working");

        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("AI validation fallback triggered for malformed response")
    void aiValidationFallback_whenMalformed() throws Exception {
        TriageRequest request = new TriageRequest();
        request.setQuestion("test");
        request.setAge(1);
        request.setGender("male");
        request.setSymptoms(java.util.List.of("symptom"));
        request.setSeverity("mild");
        request.setDuration("1 day");
        request.setContext("test");

        mockMvc.perform(post("/api/v1/triage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.clinicalGuidance")
                .value("Safety override: AI response could not be validated. Please consult a qualified healthcare professional."));
    }
}