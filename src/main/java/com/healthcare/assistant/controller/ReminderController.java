package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.ReminderScheduleRequest;
import com.healthcare.assistant.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @PostMapping("/schedule")
    public ResponseEntity<Void> scheduleReminder(@RequestBody ReminderScheduleRequest request) {
        reminderService.scheduleReminder(request.getPatientId(), request.getReminderType(), request.getScheduledDateTime());
        return ResponseEntity.noContent().build();
    }
}