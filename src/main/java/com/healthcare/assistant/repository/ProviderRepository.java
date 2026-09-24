package com.healthcare.assistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.healthcare.assistant.entity.Provider;
import java.util.List;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Provider findByLicenseNumber(String licenseNumber);
    List<Provider> findBySpecialty(String specialty);
}