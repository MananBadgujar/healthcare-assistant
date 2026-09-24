package com.healthcare.notificationservice.web;

import com.healthcare.notificationservice.entity.Notification;
import com.healthcare.notificationservice.repo.NotificationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationRepository repo;
    public NotificationController(NotificationRepository repo) { this.repo = repo; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER','PATIENT')")
    public List<Notification> all() {
        return repo.findAll();
    }
}
