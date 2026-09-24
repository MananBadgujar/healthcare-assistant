package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentStatusController {

    @Autowired
    private com.healthcare.assistant.service.AppointmentService appointmentService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestBody StatusUpdateRequest request) {
        appointmentService.updateStatus(id, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    // DTO for status update
    public static class StatusUpdateRequest {
        private String status;

        public StatusUpdateRequest() {}

        public StatusUpdateRequest(String status) {
            this.status = status;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}