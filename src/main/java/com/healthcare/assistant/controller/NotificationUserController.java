package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

import java.util.List;


@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationUserController {

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<String>> getUserNotifications(@PathVariable Long userId) {
        // TODO: implement retrieval logic
        return ResponseEntity.ok(Collections.emptyList());
    }
}