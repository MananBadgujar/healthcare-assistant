package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.TelehealthSessionDto;
import com.healthcare.assistant.entity.TelehealthSession;
import com.healthcare.assistant.service.TelehealthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telehealth")
public class TelehealthController {

    private final TelehealthService telehealthService;

    @Autowired
    public TelehealthController(TelehealthService telehealthService) {
        this.telehealthService = telehealthService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<TelehealthSessionDto> createSession(@Valid @RequestBody TelehealthSessionDto sessionDto) {
        TelehealthSession session = new TelehealthSession();
        session.setScheduledStart(sessionDto.getScheduledStart());
        session.setScheduledEnd(sessionDto.getScheduledEnd());
        session.setStatus("SCHEDULED");
        TelehealthSession created = telehealthService.createSession(session);
        return ResponseEntity.status(201).body(new TelehealthSessionDto(created));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<TelehealthSessionDto> getSession(@PathVariable Long id) {
        TelehealthSession session = telehealthService.getSessionById(id);
        return session != null ? ResponseEntity.ok(new TelehealthSessionDto(session)) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/sessions/{id}/status")
    public ResponseEntity<TelehealthSessionDto> updateSessionStatus(@PathVariable Long id, @RequestBody String newStatus) {
        TelehealthSession session = telehealthService.updateSessionStatus(id, newStatus);
        return ResponseEntity.ok(new TelehealthSessionDto(session));
    }

    @GetMapping("/sessions/patient/{patientId}")
    public List<TelehealthSessionDto> getSessionsByPatient(@PathVariable Long patientId) {
        return telehealthService.getSessionsByPatientId(patientId).stream()
                .map(TelehealthSessionDto::new)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/sessions/provider/{providerId}")
    public List<TelehealthSessionDto> getSessionsByProvider(@PathVariable Long providerId) {
        return telehealthService.getSessionsByProviderId(providerId).stream()
                .map(TelehealthSessionDto::new)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/sessions/status/{status}")
    public List<TelehealthSessionDto> getSessionsByStatus(@PathVariable String status) {
        return telehealthService.getSessionsByStatus(status).stream()
                .map(TelehealthSessionDto::new)
                .collect(java.util.stream.Collectors.toList());
    }
}