package com.healthcare.assistant.controller;

import java.util.Collections;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryStockLevelsController {

    @GetMapping("/stock-levels")
    public ResponseEntity<List<String>> getStockLevels() {
        // TODO: implement stock level retrieval
        return ResponseEntity.ok(Collections.emptyList());
    }
}