package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.entity.TelehealthSession;
import com.healthcare.assistant.repository.TelehealthSessionRepository;
import com.healthcare.assistant.repository.AppointmentRepository;
import com.healthcare.assistant.service.TelehealthService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class TelehealthServiceImpl implements TelehealthService {

    @Autowired
    private TelehealthSessionRepository sessionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public TelehealthSession createSession(TelehealthSession session) {
        Long patientId = session.getPatient().getId();
        Long providerId = session.getProvider().getId();

        Patient patient = session.getPatient();
        Provider provider = session.getProvider();

        if (session.getAppointment() != null) {
            Appointment appointment = appointmentRepository.findById(session.getAppointment().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + session.getAppointment().getId()));
            if (!appointment.getPatient().getId().equals(patientId)) {
                throw new IllegalArgumentException("Appointment does not belong to the specified patient");
            }
            if (!appointment.getProvider().getId().equals(providerId)) {
                throw new IllegalArgumentException("Appointment does not belong to the specified provider");
            }
        }

        if (patientId == null || providerId == null) {
            throw new IllegalArgumentException("Patient and provider must not be null");
        }

        session.setStatus("SCHEDULED");
        session.setActualStart(null);
        session.setActualEnd(null);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setMeetingUrl("/telehealth/meeting/" + session.getId());
        session.setSessionReference(UUID.randomUUID().toString());

        return sessionRepository.save(session);
    }

    @Override
    public TelehealthSession getSessionById(Long id) {
        Optional<TelehealthSession> optional = sessionRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<TelehealthSession> getSessionsByPatientId(Long patientId) {
        return sessionRepository.findByPatientIdOrderByScheduledStartDesc(patientId);
    }

    @Override
    public List<TelehealthSession> getSessionsByProviderId(Long providerId) {
        return sessionRepository.findByProviderIdOrderByScheduledStartDesc(providerId);
    }

    @Override
    public List<TelehealthSession> getSessionsByStatus(String status) {
        return sessionRepository.findByStatusOrderByScheduledStart(status);
    }

    @Override
    @Transactional
    public TelehealthSession updateSessionStatus(Long id, String newStatus) {
        TelehealthSession session = sessionRepository.findById(id).orElseThrow();
        session.setStatus(newStatus);
        if (newStatus.equals("IN_PROGRESS") && session.getActualStart() == null) {
            session.setActualStart(LocalDateTime.now());
        }
        if (newStatus.equals("COMPLETED") && session.getActualEnd() == null) {
            session.setActualEnd(LocalDateTime.now());
        }
        if (newStatus.equals("CLOSED") && session.getActualEnd() != null) {
            session.setUpdatedAt(LocalDateTime.now());
        }
        return sessionRepository.save(session);
    }

    @Override
    public boolean isValidTransition(String fromStatus, String toStatus) {
        // Define valid lifecycle transitions
        // SCHEDULED -> READY -> IN_PROGRESS -> COMPLETED -> CLOSED
        // Also: CANCELLED, NO_SHOW

        if (fromStatus == null) {
            return toStatus != null;
        }

        switch (fromStatus) {
            case "SCHEDULED":
                return toStatus.equals("READY") || toStatus.equals("CANCELLED") || toStatus.equals("NO_SHOW");
            case "READY":
                return toStatus.equals("IN_PROGRESS") || toStatus.equals("CANCELLED") || toStatus.equals("NO_SHOW");
            case "IN_PROGRESS":
                return toStatus.equals("COMPLETED") || toStatus.equals("CANCELLED") || toStatus.equals("NO_SHOW");
            case "COMPLETED":
                return toStatus.equals("CLOSED");
            case "CLOSED":
                return false; // Cannot transition from CLOSED
            case "CANCELLED":
                return false; // Cannot transition from CANCELLED
            case "NO_SHOW":
                return false; // Cannot transition from NO_SHOW
            default:
                return false;
        }
    }
}