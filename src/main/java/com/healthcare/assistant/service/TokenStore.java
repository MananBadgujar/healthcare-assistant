package com.healthcare.assistant.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TokenStore {

    private Set<String> validRefreshTokens = new HashSet<>();

    public void addToken(String token) {
        validRefreshTokens.add(token);
    }

    public boolean containsToken(String token) {
        return validRefreshTokens.contains(token);
    }

    public void removeToken(String token) {
        validRefreshTokens.remove(token);
    }
}
