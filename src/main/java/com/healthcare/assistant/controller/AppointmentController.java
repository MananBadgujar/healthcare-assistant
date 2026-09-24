package com.healthcare.assistant.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.service.AppointmentService;
import com.healthcare.assistant.service.PatientContextService;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientContextService patientContextService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        Appointment savedAppointment = appointmentService.saveAppointment(appointment);
        return ResponseEntity.ok(savedAppointment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        if (appointment.isPresent()) {
            Long appointmentPatientId = appointment.get().getPatient().getId();
            Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
            if (!appointmentPatientId.equals(currentPatientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return appointment.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointmentDetails) {
        Optional<Appointment> optionalAppointment = appointmentService.getAppointmentById(id);
        if (!optionalAppointment.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Appointment appointment = optionalAppointment.get();

        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!appointment.getPatient().getId().equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        appointment.setPatient(appointmentDetails.getPatient());
        appointment.setProvider(appointmentDetails.getProvider());
        appointment.setAppointmentDateTime(appointmentDetails.getAppointmentDateTime());
        appointment.setReason(appointmentDetails.getReason());
        appointment.setNotes(appointmentDetails.getNotes());
        appointment.setStatus(appointmentDetails.getStatus());

        appointmentService.saveAppointment(appointment);
        return ResponseEntity.ok(appointment);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = appointmentService.getAppointmentById(id);
        if (!optionalAppointment.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Long appointmentPatientId = optionalAppointment.get().getPatient().getId();
        Long currentPatientId = patientContextService.getCurrentPatientContext().getId();
        if (!appointmentPatientId.equals(currentPatientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}