package com.healthcare.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtIssuer {
    private final Key key;
    public JwtIssuer(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
    public String access(String username, List<String> roles) {
        return Jwts.builder().setSubject(username).claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
    public String refresh(String username) {
        return Jwts.builder().setSubject(username).claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 604_800_000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
    public String subjectIfValid(String token) {
        try {
            var c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            if (c.getExpiration() != null && c.getExpiration().before(new Date()))
                throw new IllegalArgumentException("Refresh token expired");
            return c.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}
