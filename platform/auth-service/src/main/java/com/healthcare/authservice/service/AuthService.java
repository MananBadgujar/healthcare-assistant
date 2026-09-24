package com.healthcare.authservice.service;

import com.healthcare.authservice.entity.AppUser;
import com.healthcare.authservice.repo.UserRepository;
import com.healthcare.authservice.security.JwtIssuer;
import com.healthcare.authservice.common.AuditService;
import com.healthcare.authservice.common.EventPublisher;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {
    private static final Set<String> ROLES = Set.of("ADMIN", "PROVIDER", "PATIENT", "STAFF");
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtIssuer issuer;
    private final AuditService audit;
    private final EventPublisher events;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtIssuer issuer,
                       AuditService audit, EventPublisher events) {
        this.users = users; this.encoder = encoder; this.issuer = issuer;
        this.audit = audit; this.events = events;
    }

    @Transactional
    public AppUser register(String username, String password, String role) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password must be >= 8 chars");
        String r = (role == null) ? "PATIENT" : role.toUpperCase();
        if (!ROLES.contains(r)) throw new IllegalArgumentException("Unknown role: " + role);
        if (users.findByUsername(username).isPresent()) throw new IllegalArgumentException("Username already exists");
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(r);
        AppUser saved = users.save(u);
        audit.record(username, "REGISTER", "User", String.valueOf(saved.getId()), "SUCCESS");
        try {
            events.publish("healthcare.auth.events.registered",
                DomainEvent.of("auth.user.registered", "User", String.valueOf(saved.getId()),
                    "auth-service", MDC.get("correlationId"),
                    Map.of("username", username, "role", r)));
        } catch (Exception ignored) {}
        return saved;
    }

    public Map<String, String> login(String username, String password) {
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(password, u.getPasswordHash())) {
            audit.record(username, "LOGIN", "User", String.valueOf(u.getId()), "FAILED");
            throw new IllegalArgumentException("Invalid credentials");
        }
        audit.record(username, "LOGIN", "User", String.valueOf(u.getId()), "SUCCESS");
        String access = issuer.access(u.getUsername(), List.of(u.getRole()));
        String refresh = issuer.refresh(u.getUsername());
        return Map.of("accessToken", access, "refreshToken", refresh, "role", u.getRole());
    }

    public Map<String, String> refresh(String refreshToken, JwtIssuer issuer) {
        String username = issuer.subjectIfValid(refreshToken);
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        return Map.of("accessToken", issuer.access(u.getUsername(), List.of(u.getRole())));
    }
}
