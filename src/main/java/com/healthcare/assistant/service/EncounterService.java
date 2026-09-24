package com.healthcare.assistant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthcare.assistant.entity.Encounter;
import com.healthcare.assistant.repository.EncounterRepository;

@Service
public class EncounterService {

    @Autowired
    private EncounterRepository encounterRepository;

    public Encounter saveEncounter(Encounter encounter) {
        return encounterRepository.save(encounter);
    }

    public Optional<Encounter> getEncounterById(Long id) {
        return encounterRepository.findById(id);
    }

    public List<Encounter> getAllEncounters() {
        return encounterRepository.findAll();
    }

    public void deleteEncounter(Long id) {
        encounterRepository.deleteById(id);
    }
}
