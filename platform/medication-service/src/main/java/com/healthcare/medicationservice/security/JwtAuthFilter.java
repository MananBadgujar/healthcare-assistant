package com.healthcare.medicationservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;
    public JwtAuthFilter(JwtUtil jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                if (jwt.valid(token)) {
                    Claims c = jwt.parse(token);
                    List<SimpleGrantedAuthority> auths = jwt.roles(c).stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                    UsernamePasswordAuthenticationToken at =
                            new UsernamePasswordAuthenticationToken(c.getSubject(), null, auths);
                    at.setDetails(c);
                    SecurityContextHolder.getContext().setAuthentication(at);
                    req.setAttribute("username", c.getSubject());
                    req.setAttribute("roles", jwt.roles(c));
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
