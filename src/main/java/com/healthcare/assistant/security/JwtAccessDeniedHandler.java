package com.healthcare.assistant.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles requests from authenticated principals that lack the required role
 * (for example a {@code PATIENT} hitting {@code @PreAuthorize}
 * admin-only endpoints). Returns HTTP 403 Forbidden so callers can
 * distinguish "no/invalid token" (401) from "valid token but unauthorized"
 * (403). The rest of the security model (JWT validation, public matchers) is
 * unchanged.
 */
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"error\":\"Forbidden\"}");
        }
    }
}
