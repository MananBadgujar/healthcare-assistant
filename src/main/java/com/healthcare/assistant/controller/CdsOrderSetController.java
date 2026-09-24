package com.healthcare.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/cds/order-set")
public class CdsOrderSetController {

    @PostMapping
    public ResponseEntity<String> createOrderSet(@RequestBody java.util.Map<String, String> request) {
        // TODO: implement order set creation logic
        return ResponseEntity.ok("Order set created");
    }
}