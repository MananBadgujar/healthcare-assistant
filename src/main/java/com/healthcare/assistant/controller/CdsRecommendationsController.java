package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/cds/recommendations")
public class CdsRecommendationsController {

    @GetMapping("/{id}")
    public ResponseEntity<String> getRecommendation(@PathVariable Long id) {
        // TODO: implement retrieval logic
        return ResponseEntity.ok("Recommendation " + id);
    }
}