package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionAlert;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class DrugInteractionAlertService {

    private static final String[] ALERT_LEVEL_ORDER = {"URGENT", "HIGH", "MODERATE", "LOW"};

    /**
     * Prioritizes drug interaction alerts based on alert level and generates
     * actionable text.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param alerts list of drug interaction alerts to prioritize
     * @return the most prioritized alert with actionable text, or null if list is empty
     */
    public DrugInteractionAlert prioritizeAlerts(Medication medicationA,
                                                 Medication medicationB,
                                                 List<DrugInteractionAlert> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            return null;
        }

        alerts.sort(Comparator.comparing((DrugInteractionAlert da) -> da.getAlertLevel())
                .reversed());

        DrugInteractionAlert topAlert = alerts.get(0);
        topAlert.setAlertText(generateAlertText(topAlert));
        topAlert.setActionRequired(generateActionRequired(topAlert));
        topAlert.setUpdatedAt(LocalDateTime.now());

        return topAlert;
    }

    private String generateAlertText(DrugInteractionAlert alert) {
        StringBuilder text = new StringBuilder();
        text.append("Drug interaction between ");
        text.append(alert.getMedicationA().getName()).append(" and ");
        text.append(alert.getMedicationB().getName()).append(": ");
        text.append(alert.getAlertText() != null ? alert.getAlertText() : "No description available.");
        return text.toString();
    }

    private String generateActionRequired(DrugInteractionAlert alert) {
        String level = alert.getAlertLevel();
        switch (level) {
            case "URGENT":
                return "Immediate provider review required. Stop medications if adverse events occur.";
            case "HIGH":
                return "Prompt provider review required. Adjust therapy or monitor closely.";
            case "MODERATE":
                return "Review therapy and monitor for symptoms. Consider alternative medications.";
            case "LOW":
                return "Monitor for interactions. Continue current therapy with awareness.";
            default:
                return "Review interaction and monitor patient.";
        }
    }

    /**
     * Compares alert severity levels.
     *
     * @param a first alert level
     * @param b second alert level
     * @return negative if a > b, positive if a < b
     */
    private int compareAlertLevel(String a, String b) {
        int idxA = -1, idxB = -1;
        for (int i = 0; i < ALERT_LEVEL_ORDER.length; i++) {
            if (ALERT_LEVEL_ORDER[i].equals(a)) idxA = i;
            if (ALERT_LEVEL_ORDER[i].equals(b)) idxB = i;
        }
        return Integer.compare(idxA, idxB);
    }
}