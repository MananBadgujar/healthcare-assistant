package com.healthcare.cdsservice.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CdsService {
    private static final Map<String, String> PAIRS = Map.of(
            "aspirin+warfarin", "MAJOR",
            "ibuprofen+warfarin", "MAJOR",
            "clarithromycin+simvastatin", "MAJOR",
            "contrast+metformin", "MODERATE",
            "lisinopril+potassium", "MODERATE");

    public Map<String, Object> check(List<String> drugs) {
        if (drugs == null || drugs.size() < 2) throw new IllegalArgumentException("At least two drugs required");
        List<String> norm = drugs.stream().map(d -> d.toLowerCase().trim()).sorted().toList();
        List<Map<String, String>> alerts = new ArrayList<>();
        for (int i = 0; i < norm.size(); i++) for (int j = i + 1; j < norm.size(); j++) {
            String key = norm.get(i) + "+" + norm.get(j);
            String sev = PAIRS.get(key);
            if (sev != null) alerts.add(Map.of("pair", key, "severity", sev,
                    "evidence", "CDS knowledge base v1"));
        }
        String risk = alerts.stream().anyMatch(a -> a.get("severity").equals("MAJOR")) ? "HIGH"
                : alerts.isEmpty() ? "LOW" : "MODERATE";
        return Map.of("alerts", alerts, "risk", risk,
                "recommendation", alerts.isEmpty() ? "No known interactions." : "Provider review required before ordering.",
                "requiresProviderReview", true);
    }
}
