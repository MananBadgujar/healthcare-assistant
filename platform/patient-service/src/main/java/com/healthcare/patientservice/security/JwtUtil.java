package com.healthcare.patientservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("jwt.secret missing");
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean valid(String token) {
        try {
            Claims c = parse(token);
            return c.getExpiration() == null || c.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims c) {
        Object r = c.get("roles");
        if (r instanceof List) {
            List<String> out = new ArrayList<>();
            for (Object o : (List<?>) r) out.add(String.valueOf(o));
            return out;
        }
        Object single = c.get("role");
        if (single != null) return List.of(String.valueOf(single));
        return List.of();
    }
}
