package com.healthcare.appointmentservice.web;

import com.healthcare.appointmentservice.entity.Appointment;
import com.healthcare.appointmentservice.repo.AppointmentRepository;
import com.healthcare.appointmentservice.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final AppointmentRepository repo;
    public AppointmentController(AppointmentService service, AppointmentRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Appointment> book(@RequestBody Appointment a, Authentication auth,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.book(a, auth.getName(), authHeader, idemKey));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Appointment get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Appointment not found"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Appointment status(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        return service.changeStatus(id, body.get("status"), auth.getName());
    }
}
