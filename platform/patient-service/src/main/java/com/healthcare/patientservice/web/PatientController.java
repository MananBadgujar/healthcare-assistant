package com.healthcare.patientservice.web;

import com.healthcare.patientservice.entity.Patient;
import com.healthcare.patientservice.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {
    private final PatientService service;
    public PatientController(PatientService service) { this.service = service; }

    private boolean privileged(Authentication a) {
        return a.getAuthorities().stream().anyMatch(g ->
                g.getAuthority().equals("ROLE_ADMIN") || g.getAuthority().equals("ROLE_PROVIDER")
                        || g.getAuthority().equals("ROLE_STAFF"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Patient> create(@RequestBody Patient p, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p, auth.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Patient> get(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.get(id, auth.getName(), privileged(auth)));
    }

    @PatchMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public ResponseEntity<Patient> update(@PathVariable Long id, @RequestBody Patient patch) {
        return ResponseEntity.ok(service.update(id, patch));
    }
}
