package com.healthcare.assistant.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter implements Filter {

    // Endpoint patterns that require rate limiting
    private static final String[] RATE_LIMITED_PATTERNS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    };

    // Rate limit configuration: max requests per period
    private static final int MAX_REQUESTS = 5;
    private static final long DURATION_SECONDS = 60;

    // In-memory map to track request counts per IP
    private final Map<String, int[]> ipCounters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestPath = request.getRequestURI();

        // Check if this endpoint requires rate limiting
        boolean isRateLimited = false;
        for (String pattern : RATE_LIMITED_PATTERNS) {
            if (pattern.equals(requestPath)) {
                isRateLimited = true;
                break;
            }
        }

        if (isRateLimited) {
            String ip = request.getRemoteAddr();
            int[] counter = ipCounters.get(ip);

            if (counter == null) {
                counter = new int[2]; // [count, lastResetSecond]
                ipCounters.put(ip, counter);
            }

            long currentSecond = Instant.now().getEpochSecond() / DURATION_SECONDS;
            if (counter[1] < currentSecond) {
                // Reset counter for new minute window
                counter[0] = 0;
                counter[1] = (int) currentSecond;
            }

            counter[0]++;

            if (counter[0] > MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                try {
                    response.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum " + MAX_REQUESTS + " requests per minute allowed.\"}");
                } catch (IOException e) {
                    // ignore
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}