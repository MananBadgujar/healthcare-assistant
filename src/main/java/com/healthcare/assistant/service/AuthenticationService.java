package com.healthcare.assistant.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.assistant.dto.ChangePasswordRequest;
import com.healthcare.assistant.dto.LoginRequest;
import com.healthcare.assistant.dto.LoginResponse;
import com.healthcare.assistant.dto.LogoutRequest;
import com.healthcare.assistant.dto.RefreshTokenRequest;
import com.healthcare.assistant.dto.RefreshTokenResponse;
import com.healthcare.assistant.dto.RegisterRequest;
import com.healthcare.assistant.dto.RegisterResponse;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.repository.UserRepository;
import com.healthcare.assistant.security.JwtTokenUtil;
import com.healthcare.assistant.kafka.audit.EventAudit;
import com.healthcare.assistant.kafka.event.EventEnvelope;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventAudit eventAudit;

    private Set<String> tokenBlacklist = new HashSet<>();
    private Set<String> validRefreshTokens = new HashSet<>();
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public RegisterResponse registerUser(RegisterRequest request) {
        Optional<User> existingUserOptional = userRepository.findByEmail(request.getEmail());

        if (existingUserOptional.isPresent()) {
            return new RegisterResponse("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        // Audit: registration event
        String correlationId = UUID.randomUUID().toString();
        eventAudit.record(
                new EventEnvelope("user.registered", "user", String.valueOf(user.getId()),
                        "auth-service", "Registration successful"),
                "SUCCESS", correlationId);

        return new RegisterResponse("User registered successfully");
    }


    public LoginResponse loginUser(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (!userOptional.isPresent()) {
            // Audit: failed login attempt
            String correlationId = UUID.randomUUID().toString();
            eventAudit.record(
                    new EventEnvelope("login.attempt", "auth", correlationId,
                            "auth-service", "Failed login: unknown user"),
                    "FAILED", correlationId);
            return new LoginResponse(null, null, "Invalid email or password");
        }

        User user = userOptional.get();
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            // Audit: failed login attempt
            String correlationId = UUID.randomUUID().toString();
            eventAudit.record(
                    new EventEnvelope("login.attempt", "auth", correlationId,
                            "auth-service", "Failed login: password mismatch"),
                    "FAILED", correlationId);
            return new LoginResponse(null, null, "Invalid email or password");
        }

        String accessToken = jwtTokenUtil.generateToken(user.getEmail());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());

        validRefreshTokens.add(refreshToken);

        // Audit: successful login
        String correlationId = UUID.randomUUID().toString();
        eventAudit.record(
                new EventEnvelope("login.success", "user", String.valueOf(user.getId()),
                        "auth-service", "Login successful"),
                "SUCCESS", correlationId);

        return new LoginResponse(accessToken, refreshToken, "Login successful");
    }


    public void logoutUser(LogoutRequest request) {
        String token = request.getToken();
        if (token != null && !token.isBlank()) {
            tokenBlacklist.add(token);
            // Audit: logout event
            String correlationId = UUID.randomUUID().toString();
            eventAudit.record(
                    new EventEnvelope("logout", "user", correlationId,
                            "auth-service", "User logout"),
                    "SUCCESS", correlationId);
        } else {
            System.out.println("No token provided for logout.");
        }
    }


    public void addValidRefreshToken(String token) {
        validRefreshTokens.add(token);
    }

    public void removeRefreshToken(String token) {
        validRefreshTokens.remove(token);
    }
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || !validRefreshTokens.contains(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        // refreshTokenからusernameを取得 (jwtTokenUtilを使用)
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

        // usernameから新しいaccess tokenを生成
        String newAccessToken = jwtTokenUtil.generateToken(username);

        return new RefreshTokenResponse(newAccessToken);
    }




    public void changePassword(ChangePasswordRequest request) {
        // Password change logic
    }
}