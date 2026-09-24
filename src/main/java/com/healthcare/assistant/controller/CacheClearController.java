package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cache")
public class CacheClearController {

    @PostMapping("/clear")
    public ResponseEntity<Void> clearCache() {
        // TODO: implement cache clearing logic
        return ResponseEntity.noContent().build();
    }
}