package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Notification;
import com.healthcare.assistant.repository.NotificationRepository;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public void sendNow(String patientId, String message) {
        // Validate patientId numeric
        Long pid;
        try {
            pid = Long.parseLong(patientId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid patientId");
        }
        Optional<Patient> patientOpt = patientRepository.findById(pid);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found for id: " + pid);
        }
        Patient patient = patientOpt.get();
        Notification notif = new Notification(patient, message);
        notificationRepository.save(notif);
    }
}