package com.healthcare.gateway.proxy;

import com.healthcare.contracts.ApiError;
import com.healthcare.contracts.Correlation;
import com.healthcare.gateway.routing.RouteTable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class ProxyController {
    private static final Set<String> HOP_HEADERS = Set.of(
            "host", "content-length", "transfer-encoding", "connection");
    private final RouteTable routes;
    private final RestTemplate rest;

    public ProxyController(RouteTable routes, RestTemplateBuilder builder,
                           @Value("${gw.timeout-ms:5000}") int timeoutMs) {
        this.routes = routes;
        this.rest = builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @RequestMapping("/api/**")
    public ResponseEntity<?> proxy(HttpServletRequest req) throws Exception {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String target = routes.targetFor(uri);
        String url = target + uri + (query == null ? "" : "?" + query);

        byte[] body = req.getInputStream().readAllBytes();
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            if (HOP_HEADERS.contains(n.toLowerCase())) continue;
            headers.put(n, java.util.Collections.list(req.getHeaders(n)));
        }
        Object cid = req.getAttribute(Correlation.HEADER);
        if (cid != null) headers.set(Correlation.HEADER, String.valueOf(cid));

        HttpMethod method = HttpMethod.valueOf(req.getMethod());
        try {
            ResponseEntity<byte[]> downstream =
                    rest.exchange(url, method, new HttpEntity<>(body, headers), byte[].class);
            HttpHeaders out = new HttpHeaders();
            String ct = downstream.getHeaders().getFirst("Content-Type");
            if (ct != null) out.set("Content-Type", ct);
            if (cid != null) out.set(Correlation.HEADER, String.valueOf(cid));
            return new ResponseEntity<>(downstream.getBody(), out, downstream.getStatusCode());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .header("Content-Type", "application/json")
                    .body(e.getResponseBodyAsByteArray());
        } catch (ResourceAccessException e) {
            Object c = req.getAttribute(Correlation.HEADER);
            return ResponseEntity.status(503).body(new ApiError(503, "DOWNSTREAM_UNAVAILABLE",
                    "Downstream service unavailable: " + target,
                    c == null ? null : String.valueOf(c), "api-gateway"));
        }
    }

    @RequestMapping("/gateway/health")
    public Map<String, String> health() {
        return Map.of("service", "api-gateway", "status", "UP");
    }
}
