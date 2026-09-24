package com.healthcare.authservice.web;

import com.healthcare.authservice.entity.AppUser;
import com.healthcare.authservice.security.JwtIssuer;
import com.healthcare.authservice.service.AuthService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    private final JwtIssuer issuer;
    public AuthController(AuthService auth, JwtIssuer issuer) { this.auth = auth; this.issuer = issuer; }

    public record RegisterReq(@NotBlank String username, @NotBlank String password, String role) {}
    public record LoginReq(@NotBlank String username, @NotBlank String password) {}
    public record RefreshReq(@NotBlank String refreshToken) {}

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterReq req) {
        AppUser u = auth.register(req.username(), req.password(), req.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(auth.login(req.username(), req.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshReq req) {
        return ResponseEntity.ok(auth.refresh(req.refreshToken(), issuer));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("username", authentication.getName(),
                "roles", authentication.getAuthorities().stream().map(Object::toString).toList()));
    }
}
