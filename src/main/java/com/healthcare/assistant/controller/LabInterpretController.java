package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.LabInterpretRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/labs")
public class LabInterpretController {

    @PostMapping("/interpret")
    public ResponseEntity<String> interpret(@RequestBody LabInterpretRequest request) {
        // TODO: implement lab interpretation logic
        return ResponseEntity.ok("Interpretation result for " + request.getTestName());
    }
}