package com.healthcare.gateway;

import com.healthcare.gateway.routing.RouteTable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouteTableTest {
    RouteTable table() {
        return new RouteTable("A", "PAT", "PROV", "APPT", "MED", "BILL", "AI", "RAG",
                "CDS", "NOTIF", "INV", "MONO");
    }

    @Test
    public void routesResolveToOwningService() {
        RouteTable t = table();
        assertEquals("A", t.targetFor("/api/v1/auth/login"));
        assertEquals("PAT", t.targetFor("/api/v1/patients/1"));
        assertEquals("PROV", t.targetFor("/api/v1/providers/search"));
        assertEquals("APPT", t.targetFor("/api/v1/appointments"));
        assertEquals("APPT", t.targetFor("/api/v1/encounters/5"));
        assertEquals("MED", t.targetFor("/api/v1/medications/patient/1"));
        assertEquals("BILL", t.targetFor("/api/v1/billing/invoices"));
        assertEquals("BILL", t.targetFor("/api/v1/claims/2"));
        assertEquals("AI", t.targetFor("/api/v1/ai/triage"));
        assertEquals("AI", t.targetFor("/api/v1/triage/score"));
        assertEquals("RAG", t.targetFor("/api/v1/rag/search"));
        assertEquals("RAG", t.targetFor("/api/v1/kb/articles"));
        assertEquals("CDS", t.targetFor("/api/v1/cds/check-interactions"));
        assertEquals("NOTIF", t.targetFor("/api/v1/notifications"));
        assertEquals("INV", t.targetFor("/api/v1/inventory/items"));
        assertEquals("INV", t.targetFor("/api/v1/telehealth/sessions"));
    }

    @Test
    public void unknownPathsFallBackToMonolith() {
        assertEquals("MONO", table().targetFor("/api/v1/records/1"));
    }
}
