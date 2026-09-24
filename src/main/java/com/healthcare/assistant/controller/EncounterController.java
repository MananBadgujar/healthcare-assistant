package com.healthcare.assistant.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.assistant.entity.Encounter;
import com.healthcare.assistant.service.EncounterService;

@RestController
@RequestMapping("/api/v1/encounters")
public class EncounterController {

    @Autowired
    private EncounterService encounterService;

    @PostMapping
    public ResponseEntity<Encounter> createEncounter(@RequestBody Encounter encounter) {
        Encounter savedEncounter = encounterService.saveEncounter(encounter);
        return ResponseEntity.ok(savedEncounter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Encounter> getEncounterById(@PathVariable Long id) {
        Optional<Encounter> encounter = encounterService.getEncounterById(id);
        return encounter.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Encounter> getAllEncounters() {
        return encounterService.getAllEncounters();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Encounter> updateEncounter(@PathVariable Long id, @RequestBody Encounter encounter) {
        encounter.setId(id);
        Encounter updatedEncounter = encounterService.saveEncounter(encounter);
        return ResponseEntity.ok(updatedEncounter);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEncounter(@PathVariable Long id) {
        encounterService.deleteEncounter(id);
        return ResponseEntity.noContent().build();
    }
}
