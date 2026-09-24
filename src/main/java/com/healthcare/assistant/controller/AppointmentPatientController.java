package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Appointment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments/patient")
public class AppointmentPatientController {

    @GetMapping("/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(@PathVariable Long patientId) {
        // TODO: implement retrieval logic
        return ResponseEntity.ok(Collections.emptyList());
    }
}