package com.healthcare.appointmentservice.service;

import com.healthcare.appointmentservice.common.AuditService;
import com.healthcare.appointmentservice.common.EventPublisher;
import com.healthcare.appointmentservice.common.ServiceClients;
import com.healthcare.appointmentservice.entity.Appointment;
import com.healthcare.appointmentservice.repo.AppointmentRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class AppointmentService {
    private static final Set<String> TRANSITIONS = Set.of(
            "PENDING>CONFIRMED", "PENDING>CANCELLED", "CONFIRMED>CANCELLED",
            "CONFIRMED>COMPLETED", "PENDING>COMPLETED");
    private final AppointmentRepository repo;
    private final AuditService audit;
    private final EventPublisher events;
    private final ServiceClients clients;
    private final String providerBaseUrl;

    public AppointmentService(AppointmentRepository repo, AuditService audit, EventPublisher events,
                              ServiceClients clients,
                              @Value("${provider.service.base-url}") String providerBaseUrl) {
        this.repo = repo; this.audit = audit; this.events = events;
        this.clients = clients; this.providerBaseUrl = providerBaseUrl;
    }

    @Transactional
    public Appointment book(Appointment a, String requester, String authHeader, String idemKey) {
        if (a.getProviderId() == null) throw new IllegalArgumentException("providerId required");
        if (a.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        if (idemKey != null && !idemKey.isBlank()) {
            var existing = repo.findByIdempotencyKey(idemKey);
            if (existing.isPresent()) return existing.get();
        }
        // Controlled synchronous validation: provider must exist (timeout + circuit breaker inside).
        try {
            clients.get(providerBaseUrl, "/api/v1/providers/" + a.getProviderId(), authHeader);
        } catch (Exception e) {
            throw new IllegalArgumentException("Provider validation failed: unavailable or not found");
        }
        a.setId(null);
        a.setStatus("PENDING");
        a.setIdempotencyKey(idemKey);
        Appointment saved = repo.save(a);
        audit.record(requester, "BOOK", "Appointment", String.valueOf(saved.getId()), "SUCCESS");
        events.publish(Topics.APPOINTMENT_CREATED, DomainEvent.of("appointment.created", "Appointment",
                    String.valueOf(saved.getId()), "appointment-service", MDC.get("correlationId"),
                    Map.of("patientId", String.valueOf(saved.getPatientId()),
                            "providerId", String.valueOf(saved.getProviderId()),
                            "status", saved.getStatus())));
        return saved;
    }

    @Transactional
    public Appointment changeStatus(Long id, String next, String requester) {
        Appointment a = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Appointment not found"));
        String n = next.toUpperCase();
        if (!TRANSITIONS.contains(a.getStatus() + ">" + n))
            throw new IllegalArgumentException("Illegal transition " + a.getStatus() + " -> " + n);
        a.setStatus(n);
        Appointment saved = repo.save(a);
        audit.record(requester, "STATUS_" + n, "Appointment", String.valueOf(id), "SUCCESS");
        try {
            String topic = n.equals("CANCELLED") ? Topics.APPOINTMENT_CANCELLED : Topics.APPOINTMENT_STATUS;
            events.publish(topic, DomainEvent.of("appointment." + n.toLowerCase(), "Appointment",
                    String.valueOf(id), "appointment-service", MDC.get("correlationId"),
                    Map.of("status", n)));
        } catch (Exception ignored) {}
        return saved;
    }
}
