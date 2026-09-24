package com.healthcare.providerservice.web;

import com.healthcare.providerservice.entity.Provider;
import com.healthcare.providerservice.repo.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {
    private final ProviderRepository repo;
    public ProviderController(ProviderRepository repo) { this.repo = repo; }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public List<Provider> search(@RequestParam(required = false) String specialty) {
        if (specialty == null || specialty.isBlank()) return repo.findAll();
        return repo.findBySpecialtyContainingIgnoreCase(specialty);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Provider get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Provider not found"));
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<?> availability(@PathVariable Long id) {
        Provider p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Provider not found"));
        return ResponseEntity.ok(java.util.Map.of("providerId", p.getId(), "availableSlots",
                p.getAvailableSlots() == null ? "" : p.getAvailableSlots()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Provider> create(@RequestBody Provider p) {
        if (p.getName() == null || p.getName().isBlank()) throw new IllegalArgumentException("name required");
        p.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(p));
    }
}
