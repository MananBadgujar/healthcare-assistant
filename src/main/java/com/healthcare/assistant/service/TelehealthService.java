package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.TelehealthSession;
import java.util.List;

public interface TelehealthService {
    TelehealthSession createSession(TelehealthSession session);
    TelehealthSession getSessionById(Long id);
    List<TelehealthSession> getSessionsByPatientId(Long patientId);
    List<TelehealthSession> getSessionsByProviderId(Long providerId);
    List<TelehealthSession> getSessionsByStatus(String status);
    TelehealthSession updateSessionStatus(Long id, String newStatus);
    boolean isValidTransition(String fromStatus, String toStatus);
}