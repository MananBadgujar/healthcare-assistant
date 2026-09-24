package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.AvailabilityRequest;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderAvailabilityController {

    private final ProviderService providerService;

    @Autowired
    public ProviderAvailabilityController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<Void> setAvailability(@PathVariable Long id,
                                                @Valid @RequestBody AvailabilityRequest request) {
        Optional<Provider> providerOptional = providerService.getProviderById(id);
        if (providerOptional.isPresent()) {
            Provider provider = providerOptional.get();
            provider.setAvailable(request.isAvailable());
            providerService.updateAvailability(id, request.isAvailable());
            return ResponseEntity.<Void>noContent().build();
        }
        return ResponseEntity.<Void>notFound().build();
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> getAvailability(@PathVariable Long id) {
        boolean available = providerService.isAvailable(id);
        return ResponseEntity.ok(available);
    }
}