package com.healthcare.assistant.config;

import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.util.Optional;

@Component
public class AdminUserBootstrapper implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserBootstrapper.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserBootstrapper(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking if admin user exists...");
        Optional<User> adminExists = userRepository.findAll().stream()
                .filter(u -> "admin@example.com".equalsIgnoreCase(u.getEmail()))
                .findFirst();

        if (adminExists.isPresent()) {
            log.info("Admin user already exists: {}", adminExists.get().getEmail());
            return;
        }

        User admin = new User();
        admin.setName("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        log.info("Creating admin user with email: {}", admin.getEmail());
        userRepository.save(admin);
        log.info("Admin user saved to database.");
        boolean existsAfterSave = userRepository.findByEmail("admin@example.com").isPresent();
        log.info("Admin user existence check after save: {}", existsAfterSave);
        log.info("AdminUserBootstrapper completed.");
    }
}