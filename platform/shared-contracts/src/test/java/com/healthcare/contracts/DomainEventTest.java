package com.healthcare.contracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class DomainEventTest {
    @Test
    public void envelopeSerializesWithAllRequiredFields() throws Exception {
        DomainEvent e = DomainEvent.of("appointment.created", "Appointment", "42",
                "appointment-service", "corr-1", Map.of("status", "PENDING"));
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = om.writeValueAsString(e);
        DomainEvent back = om.readValue(json, DomainEvent.class);
        assertNotNull(back.getEventId());
        assertEquals("v1", back.getEventVersion());
        assertEquals("appointment.created", back.getEventName());
        assertEquals("corr-1", back.getCorrelationId());
        assertEquals("42", back.getEntityId());
        assertNotNull(back.getOccurredAt());
    }
}
