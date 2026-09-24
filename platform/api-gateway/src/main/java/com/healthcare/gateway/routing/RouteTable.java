package com.healthcare.gateway.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/** Prefix-based route table: longest matching prefix wins, else monolith fallback. */
@Component
public class RouteTable {
    public record Route(String prefix, String target) {}

    private final List<Route> routes;
    private final String monolith;

    public RouteTable(
            @Value("${gw.auth-url}") String auth,
            @Value("${gw.patient-url}") String patient,
            @Value("${gw.provider-url}") String provider,
            @Value("${gw.appointment-url}") String appointment,
            @Value("${gw.medication-url}") String medication,
            @Value("${gw.billing-url}") String billing,
            @Value("${gw.ai-url}") String ai,
            @Value("${gw.rag-url}") String rag,
            @Value("${gw.cds-url}") String cds,
            @Value("${gw.notification-url}") String notification,
            @Value("${gw.inventory-url}") String inventory,
            @Value("${gw.monolith-url}") String monolith) {
        this.monolith = monolith;
        this.routes = List.of(
                new Route("/api/v1/auth", auth),
                new Route("/api/v1/patients", patient),
                new Route("/api/v1/providers", provider),
                new Route("/api/v1/appointments", appointment),
                new Route("/api/v1/encounters", appointment),
                new Route("/api/v1/medications", medication),
                new Route("/api/v1/billing", billing),
                new Route("/api/v1/payments", billing),
                new Route("/api/v1/claims", billing),
                new Route("/api/v1/insurance", billing),
                new Route("/api/v1/ai", ai),
                new Route("/api/v1/triage", ai),
                new Route("/api/v1/lab", ai),
                new Route("/api/v1/careplan", ai),
                new Route("/api/v1/rag", rag),
                new Route("/api/v1/knowledge", rag),
                new Route("/api/v1/kb", rag),
                new Route("/api/v1/cds", cds),
                new Route("/api/v1/drug", cds),
                new Route("/api/v1/contraindication", cds),
                new Route("/api/v1/notifications", notification),
                new Route("/api/v1/inventory", inventory),
                new Route("/api/v1/telehealth", inventory),
                new Route("/api/v1/analytics", inventory),
                new Route("/api/v1/scheduling", inventory),
                new Route("/api/v1/population", inventory));
    }

    public String targetFor(String uri) {
        Route best = null;
        for (Route r : routes) {
            if (uri.equals(r.prefix()) || uri.startsWith(r.prefix() + "/")) {
                if (best == null || r.prefix().length() > best.prefix().length()) best = r;
            }
        }
        return best == null ? monolith : best.target();
    }

    public String monolithUrl() { return monolith; }
}
