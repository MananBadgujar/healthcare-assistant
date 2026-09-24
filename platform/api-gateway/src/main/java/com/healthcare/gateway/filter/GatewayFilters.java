package com.healthcare.gateway.filter;

import com.healthcare.contracts.ApiError;
import com.healthcare.contracts.Correlation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
public class GatewayFilters extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/v1/auth/", "/actuator/health", "/actuator/info", "/gateway/health");
    private final JwtUtil jwt;
    private final ObjectMapper om;
    private final int limitPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public GatewayFilters(JwtUtil jwt, ObjectMapper om,
                          @Value("${gw.rate-limit-per-minute:100}") int limitPerMinute) {
        this.jwt = jwt; this.om = om; this.limitPerMinute = limitPerMinute;
    }

    static class Window {
        final AtomicLong minute = new AtomicLong(System.currentTimeMillis() / 60000);
        final AtomicInteger count = new AtomicInteger();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        String cid = Correlation.ensure(req.getHeader(Correlation.HEADER));
        MDC.put("correlationId", cid);
        MDC.put("service", "api-gateway");
        res.setHeader(Correlation.HEADER, cid);
        req.setAttribute(Correlation.HEADER, cid);
        logger.info("gateway " + req.getMethod() + " " + uri + " corr=" + cid);
        try {
            // Rate limiting (per client IP, fixed window).
            String ip = req.getRemoteAddr();
            Window win = windows.computeIfAbsent(ip, k -> new Window());
            long now = System.currentTimeMillis() / 60000;
            if (win.minute.get() != now) { win.minute.set(now); win.count.set(0); }
            if (win.count.incrementAndGet() > limitPerMinute) {
                writeError(req, res, 429, "RATE_LIMITED", "Too many requests");
                return;
            }
            // Authentication boundary.
            boolean pub = PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith)
                    || uri.equals("/api/v1/auth") || uri.equals("/api/v1/auth/login")
                    || uri.equals("/api/v1/auth/register") || uri.equals("/api/v1/auth/refresh");
            if (!pub && uri.startsWith("/api/")) {
                String h = req.getHeader("Authorization");
                if (h == null || !h.startsWith("Bearer ")) {
                    writeError(req, res, 401, "UNAUTHORIZED", "Missing bearer token");
                    return;
                }
                String token = h.substring(7);
                if (!jwt.valid(token)) {
                    writeError(req, res, 401, "INVALID_TOKEN", "Invalid or expired token");
                    return;
                }
                Claims c = jwt.parse(token);
                req.setAttribute("username", c.getSubject());
                req.setAttribute("roles", jwt.roles(c));
                res.setHeader("X-User", c.getSubject());
                res.setHeader("X-Roles", String.join(",", jwt.roles(c)));
            }
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }

    private void writeError(HttpServletRequest req, HttpServletResponse res, int status,
                            String code, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        Object cid = req.getAttribute(Correlation.HEADER);
        om.writeValue(res.getOutputStream(),
                new ApiError(status, code, message, cid == null ? null : String.valueOf(cid), "api-gateway"));
    }
}
