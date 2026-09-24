package com.healthcare.assistant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.healthcare.assistant.dto.ChangePasswordRequest;
import com.healthcare.assistant.dto.LoginResponse;
import com.healthcare.assistant.dto.LoginRequest;
import com.healthcare.assistant.dto.RegisterRequest;
import com.healthcare.assistant.dto.RefreshTokenRequest;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.security.JwtTokenUtil;
import com.healthcare.assistant.security.JwtTokenStore;
import com.healthcare.assistant.service.AuthenticationService;
import com.healthcare.assistant.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtTokenStore tokenStore;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationController(JwtTokenUtil jwtTokenUtil, JwtTokenStore tokenStore,
                                    UserService userService, PasswordEncoder passwordEncoder) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tokenStore = tokenStore;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.registerNewUser(request);
        String accessToken = jwtTokenUtil.generateToken(user.getEmail());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());
        tokenStore.addToken(refreshToken);

        LoginResponse response = new LoginResponse(accessToken, refreshToken, "Registration successful.");
        log.info("User registered: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());
        if (user == null) {
            log.warn("Login attempt with invalid credentials");
            return ResponseEntity.status(401).body(new LoginResponse(null, null, "Invalid Credentials"));
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordMatches) {
            log.warn("Login attempt with invalid password for email: {}", request.getEmail());
            return ResponseEntity.status(401).body(new LoginResponse(null, null, "Invalid Credentials"));
        }

        String accessToken = jwtTokenUtil.generateToken(user.getEmail());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());
        tokenStore.addToken(refreshToken);

        LoginResponse response = new LoginResponse(accessToken, refreshToken, "Login successful.");
        log.info("User logged in: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!tokenStore.containsToken(refreshToken)) {
            return ResponseEntity.status(401).body(new LoginResponse(null, null, "Invalid Refresh Token"));
        }
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        String accessToken = jwtTokenUtil.generateToken(username);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(username);
        tokenStore.addToken(newRefreshToken);

        LoginResponse response = new LoginResponse(accessToken, newRefreshToken, "Token refreshed");
        log.info("Token refreshed for user: {}", username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        boolean success = userService.changePassword(request.getEmail(), request.getOldPassword(),
                request.getNewPassword());
        if (success) {
            log.info("Password changed for user: {}", request.getEmail());
            return ResponseEntity.status(200).body("Password changed successfully");
        } else {
            log.warn("Password change failed for user: {}", request.getEmail());
            return ResponseEntity.status(400).body("Old password is incorrect");
        }
    }
}