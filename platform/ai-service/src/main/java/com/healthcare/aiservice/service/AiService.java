package com.healthcare.aiservice.service;

import com.healthcare.aiservice.common.ServiceClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private static final List<String> RED_FLAGS = List.of(
            "chest pain", "difficulty breathing", "shortness of breath", "stroke",
            "unconscious", "severe bleeding", "suicidal", "heart attack");
    private static final List<String> NON_HEALTH = List.of(
            "stock price", "football", "election", "crypto", "movie", "lottery");
    private final ServiceClients clients;
    private final String ragBase;
    private final String cdsBase;

    public AiService(ServiceClients clients,
                     @Value("${rag.service.base-url}") String ragBase,
                     @Value("${cds.service.base-url}") String cdsBase) {
        this.clients = clients; this.ragBase = ragBase; this.cdsBase = cdsBase;
    }

    public Map<String, Object> triage(String symptoms, String authHeader) {
        String lower = (symptoms == null ? "" : symptoms.toLowerCase());
        // Guardrail 1: healthcare-topic restriction
        for (String n : NON_HEALTH) {
            if (lower.contains(n))
                return Map.of("refused", true,
                        "message", "This assistant only answers healthcare-related questions.");
        }
        if (lower.isBlank())
            throw new IllegalArgumentException("symptoms text required");
        // Guardrail 2: red-flag / emergency escalation
        List<String> flags = new ArrayList<>();
        for (String r : RED_FLAGS) if (lower.contains(r)) flags.add(r);
        String urgency = flags.isEmpty() ? (lower.contains("fever") || lower.contains("pain") ? "MODERATE" : "LOW") : "EMERGENCY";
        // RAG boundary (resilient: fallback when unavailable)
        List<String> sources = new ArrayList<>();
        try {
            var resp = clients.call(ragBase, "/api/v1/rag/search?q=" + java.net.URLEncoder.encode(lower, "UTF-8"),
                    org.springframework.http.HttpMethod.GET, null, authHeader);
            sources.add("rag-service:consulted");
        } catch (Exception e) {
            sources.add("rag-service:unavailable-fallback");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("refused", false);
        out.put("urgency", urgency);
        out.put("redFlags", flags);
        out.put("escalateToEmergency", !flags.isEmpty());
        out.put("sources", sources);
        out.put("disclaimer", "AI triage support only - provider review required.");
        return out;
    }

    public Map<String, Object> labInterpret(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("lab text required");
        String lower = text.toLowerCase();
        List<String> alerts = new ArrayList<>();
        if (lower.contains("critical") || lower.contains("panic value")) alerts.add("CRITICAL_VALUE");
        if (lower.contains("high") && lower.contains("glucose")) alerts.add("HYPERGLYCEMIA_FLAG");
        return Map.of("alerts", alerts, "requiresProviderReview", true,
                "summary", alerts.isEmpty() ? "No critical flags detected." : "Flags detected: " + alerts);
    }
}
