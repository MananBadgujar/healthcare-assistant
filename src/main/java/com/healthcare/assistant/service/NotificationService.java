package com.healthcare.assistant.service;

public interface NotificationService {
    void sendNow(String patientId, String message);
}