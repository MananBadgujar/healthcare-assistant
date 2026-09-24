package com.healthcare.providerservice.repo;

import com.healthcare.providerservice.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    List<Provider> findBySpecialtyContainingIgnoreCase(String specialty);
}
