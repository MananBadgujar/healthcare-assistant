package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigReloadController {

    @PostMapping("/reload")
    public ResponseEntity<Void> reloadConfig() {
        // TODO: implement config reload logic
        return ResponseEntity.noContent().build();
    }
}