package com.healthcare.cdsservice.common;

import com.healthcare.contracts.Correlation;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controlled synchronous inter-service client with timeouts, correlation
 * propagation and a minimal circuit breaker (closed/open/half-open).
 */
@Component
public class ServiceClients {
    private final RestTemplate rest;
    private final Map<String, Breaker> breakers = new ConcurrentHashMap<>();

    public ServiceClients(RestTemplateBuilder builder) {
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    public ResponseEntity<String> get(String baseUrl, String path, String authHeader) {
        return call(baseUrl, path, HttpMethod.GET, null, authHeader);
    }

    public ResponseEntity<String> call(String baseUrl, String path, HttpMethod method,
                                       String body, String authHeader) {
        Breaker b = breakers.computeIfAbsent(baseUrl, k -> new Breaker());
        if (!b.allow()) throw new ResourceAccessException("Circuit OPEN for " + baseUrl);
        HttpHeaders h = new HttpHeaders();
        h.set("Accept", "application/json");
        h.set("Content-Type", "application/json");
        String cid = MDC.get("correlationId");
        if (cid != null) h.set(Correlation.HEADER, cid);
        if (authHeader != null) h.set("Authorization", authHeader);
        try {
            ResponseEntity<String> r = rest.exchange(baseUrl + path, method,
                    new HttpEntity<>(body, h), String.class);
            b.success();
            return r;
        } catch (Exception e) {
            b.failure();
            throw e;
        }
    }

    static class Breaker {
        private final AtomicInteger failures = new AtomicInteger();
        private volatile long openedAt;
        boolean allow() {
            if (openedAt == 0) return true;
            if (System.currentTimeMillis() - openedAt > 30_000) { openedAt = 0; failures.set(0); return true; }
            return false;
        }
        void success() { failures.set(0); openedAt = 0; }
        void failure() { if (failures.incrementAndGet() >= 5) openedAt = System.currentTimeMillis(); }
    }
}
