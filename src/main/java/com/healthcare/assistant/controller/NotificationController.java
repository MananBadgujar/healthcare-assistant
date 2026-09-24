package com.healthcare.assistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.healthcare.assistant.service.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/sendNow")
    public ResponseEntity<Void> sendNow(@RequestBody Map<String, String> request) {
        // Expecting keys: "patientId" and "message" perhaps
        String patientIdStr = request.get("patientId");
        String message = request.get("message");
        if (patientIdStr == null || message == null) {
            throw new IllegalArgumentException("patientId and message are required");
        }
        notificationService.sendNow(patientIdStr, message);
        return ResponseEntity.ok().build();
    }
}