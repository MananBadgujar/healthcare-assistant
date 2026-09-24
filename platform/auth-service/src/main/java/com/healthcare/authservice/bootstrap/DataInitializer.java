package com.healthcare.authservice.bootstrap;

import com.healthcare.authservice.entity.AppUser;
import com.healthcare.authservice.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.findByUsername("admin@example.com").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin@example.com");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole("ADMIN");
            users.save(admin);
            System.out.println("[DataInitializer] Created admin user: admin@example.com");
        } else {
            System.out.println("[DataInitializer] Admin user already exists");
        }
    }
}