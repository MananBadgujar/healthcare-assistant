package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.Reminder;
import com.healthcare.assistant.repository.ReminderRepository;
import com.healthcare.assistant.service.ReminderService;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public Reminder scheduleReminder(Long patientId, String reminderType, String scheduledDateTime) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        LocalDateTime scheduled = LocalDateTime.parse(scheduledDateTime);
        Reminder reminder = new Reminder(patient, reminderType, scheduled);
        return reminderRepository.save(reminder);
    }

    @Override
    public Reminder sendReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        if (!"PENDING".equals(reminder.getStatus())) {
            throw new IllegalStateException("Reminder already processed");
        }
        reminder.setStatus("SENT");
        reminder.setSentAt(LocalDateTime.now());
        return reminderRepository.save(reminder);
    }

    @Override
    public List<Reminder> findPendingReminders(Long patientId) {
        return reminderRepository.findByPatientId(patientId);
    }
}