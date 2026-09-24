package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/cds/drug-interactions")
public class CdsDrugInteractionsController {

    @PostMapping("/analyze")
    public ResponseEntity<String> analyze(@RequestBody java.util.Map<String, String> request) {
        // TODO: implement drug interaction analysis
        return ResponseEntity.ok("Analysis result");
    }
}