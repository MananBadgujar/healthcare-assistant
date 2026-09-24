package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Appointment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.healthcare.assistant.service.AppointmentSuggestionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentSuggestController {
    private final AppointmentSuggestionService appointmentSuggestionService;

    public AppointmentSuggestController(AppointmentSuggestionService appointmentSuggestionService) {
        this.appointmentSuggestionService = appointmentSuggestionService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<Appointment> suggestAppointment(@RequestBody Appointment request) {
        Appointment suggestion = appointmentSuggestionService.generateSuggestion();
        return ResponseEntity.ok(suggestion);
    }
}