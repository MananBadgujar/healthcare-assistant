package com.healthcare.assistant.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.service.ProviderService;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @PostMapping
    public ResponseEntity<Provider> createProvider(@Valid @RequestBody Provider provider) {
        Provider savedProvider = providerService.saveProvider(provider);
        return ResponseEntity.ok(savedProvider);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable Long id) {
        Optional<Provider> provider = providerService.getProviderById(id);
        return provider.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Provider> getAllProviders() {
        return providerService.getAllProviders();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Provider> updateProvider(@PathVariable Long id, @Valid @RequestBody Provider provider) {
        provider.setId(id);
        Provider updatedProvider = providerService.saveProvider(provider);
        return ResponseEntity.ok(updatedProvider);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        providerService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Provider>> searchProviders(@RequestParam String specialty) {
        List<Provider> providers = providerService.findBySpecialty(specialty);
        return ResponseEntity.ok(providers);
    }
}