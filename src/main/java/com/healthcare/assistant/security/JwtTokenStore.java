package com.healthcare.assistant.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenStore {
    private final Set<String> tokens = new HashSet<>();

    public void addToken(String token) {
        tokens.add(token);
    }

    public boolean containsToken(String token) {
        return token != null && tokens.contains(token);
    }

    public void removeToken(String token) {
        tokens.remove(token);
    }
}