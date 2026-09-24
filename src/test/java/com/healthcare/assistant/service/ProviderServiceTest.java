package com.healthcare.assistant.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.healthcare.assistant.entity.Provider;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class ProviderServiceTest {

    @Autowired
    private ProviderService providerService;

    @Test
    void findBySpecialty_returnsEmptyList_whenSpecialtyHasNoProviders() {
        List<Provider> providers = providerService.findBySpecialty("nonexistent_specialty");
        assertNotNull(providers);
        assertTrue(providers.isEmpty());
    }
}