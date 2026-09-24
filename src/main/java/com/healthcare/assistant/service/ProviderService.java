package com.healthcare.assistant.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.repository.ProviderRepository;

@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    public Provider saveProvider(Provider provider) {
        return providerRepository.save(provider);
    }

    public Optional<Provider> getProviderById(Long id) {
        return providerRepository.findById(id);
    }

    public Provider getProviderByLicenseNumber(String licenseNumber) {
        return providerRepository.findByLicenseNumber(licenseNumber);
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    public void deleteProvider(Long id) {
        providerRepository.deleteById(id);
    }

    public void updateAvailability(Long id, boolean available) {
        providerRepository.findById(id).ifPresent(provider -> {
            provider.setAvailable(available);
            providerRepository.save(provider);
        });
    }

    public boolean isAvailable(Long id) {
        return providerRepository.findById(id)
                .map(Provider::getAvailable)
                .orElse(false);
    }

    public List<Provider> findBySpecialty(String specialty) {
        return providerRepository.findBySpecialty(specialty);
    }
}