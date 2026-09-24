package com.healthcare.assistant.service;

import java.util.List;
import com.healthcare.assistant.entity.Reminder;

public interface ReminderService {
    Reminder scheduleReminder(Long patientId, String reminderType, String scheduledDateTime);
    Reminder sendReminder(Long reminderId);
    List<Reminder> findPendingReminders(Long patientId);
}