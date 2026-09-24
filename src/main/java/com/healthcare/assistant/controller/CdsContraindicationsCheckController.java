package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/cds/contraindications-check")
public class CdsContraindicationsCheckController {

    @PostMapping
    public ResponseEntity<String> check(@RequestBody java.util.Map<String, String> request) {
        // TODO: implement contraindication check logic
        return ResponseEntity.ok("Check result");
    }
}