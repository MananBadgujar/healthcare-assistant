"""Phase 13 scaffolder part 6: api-gateway, compose, verify script, runbook."""
import os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "platform")

def w(path, content):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)

G = "api-gateway"
P = "com.healthcare.gateway"
PD = P.replace(".", "/")

w(f"{G}/pom.xml", """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.healthcare.platform</groupId>
        <artifactId>healthcare-platform</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>api-gateway</artifactId>
    <name>API Gateway</name>
    <dependencies>
        <dependency>
            <groupId>com.healthcare.platform</groupId>
            <artifactId>shared-contracts</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
        <finalName>api-gateway</finalName>
    </build>
</project>
""")

w(f"{G}/src/main/java/{PD}/Application.java", f"""package {P};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {{
    public static void main(String[] args) {{
        SpringApplication.run(Application.class, args);
    }}
}}
""")

w(f"{G}/src/main/resources/application.properties", """server.port=8080
spring.application.name=api-gateway
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
jwt.secret=${JWT_SECRET:REDACTED}}
# Downstream targets (service names work inside docker-compose, localhost for local dev)
gw.auth-url=${AUTH_URL:http://localhost:8101}
gw.patient-url=${PATIENT_URL:http://localhost:8102}
gw.provider-url=${PROVIDER_URL:http://localhost:8103}
gw.appointment-url=${APPOINTMENT_URL:http://localhost:8104}
gw.medication-url=${MEDICATION_URL:http://localhost:8105}
gw.billing-url=${BILLING_URL:http://localhost:8106}
gw.ai-url=${AI_URL:http://localhost:8107}
gw.rag-url=${RAG_URL:http://localhost:8108}
gw.cds-url=${CDS_URL:http://localhost:8109}
gw.notification-url=${NOTIFICATION_URL:http://localhost:8110}
gw.inventory-url=${INVENTORY_URL:http://localhost:8111}
gw.monolith-url=${MONOLITH_URL:http://localhost:8081}
gw.timeout-ms=5000
gw.rate-limit-per-minute=100
""")

w(f"{G}/src/main/java/{PD}/security/JwtUtil.java", f"""package {P}.security;

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
public class JwtUtil {{
    private final Key key;
    public JwtUtil(@Value("${{jwt.secret}}") String secret) {{
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }}
    public Claims parse(String token) {{
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }}
    public boolean valid(String token) {{
        try {{
            Claims c = parse(token);
            return c.getExpiration() == null || c.getExpiration().after(new Date());
        }} catch (Exception e) {{
            return false;
        }}
    }}
    @SuppressWarnings("unchecked")
    public List<String> roles(Claims c) {{
        Object r = c.get("roles");
        if (r instanceof List) {{
            List<String> out = new ArrayList<>();
            for (Object o : (List<?>) r) out.add(String.valueOf(o));
            return out;
        }}
        Object single = c.get("role");
        if (single != null) return List.of(String.valueOf(single));
        return List.of();
    }}
}}
""")

w(f"{G}/src/main/java/{PD}/routing/RouteTable.java", f"""package {P}.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/** Prefix-based route table: longest matching prefix wins, else monolith fallback. */
@Component
public class RouteTable {{
    public record Route(String prefix, String target) {{}}

    private final List<Route> routes;
    private final String monolith;

    public RouteTable(
            @Value("${{gw.auth-url}}") String auth,
            @Value("${{gw.patient-url}}") String patient,
            @Value("${{gw.provider-url}}") String provider,
            @Value("${{gw.appointment-url}}") String appointment,
            @Value("${{gw.medication-url}}") String medication,
            @Value("${{gw.billing-url}}") String billing,
            @Value("${{gw.ai-url}}") String ai,
            @Value("${{gw.rag-url}}") String rag,
            @Value("${{gw.cds-url}}") String cds,
            @Value("${{gw.notification-url}}") String notification,
            @Value("${{gw.inventory-url}}") String inventory,
            @Value("${{gw.monolith-url}}") String monolith) {{
        this.monolith = monolith;
        this.routes = List.of(
                new Route("/api/v1/auth", auth),
                new Route("/api/v1/patients", patient),
                new Route("/api/v1/providers", provider),
                new Route("/api/v1/appointments", appointment),
                new Route("/api/v1/encounters", appointment),
                new Route("/api/v1/medications", medication),
                new Route("/api/v1/billing", billing),
                new Route("/api/v1/payments", billing),
                new Route("/api/v1/claims", billing),
                new Route("/api/v1/insurance", billing),
                new Route("/api/v1/ai", ai),
                new Route("/api/v1/triage", ai),
                new Route("/api/v1/lab", ai),
                new Route("/api/v1/careplan", ai),
                new Route("/api/v1/rag", rag),
                new Route("/api/v1/knowledge", rag),
                new Route("/api/v1/kb", rag),
                new Route("/api/v1/cds", cds),
                new Route("/api/v1/drug", cds),
                new Route("/api/v1/contraindication", cds),
                new Route("/api/v1/notifications", notification),
                new Route("/api/v1/inventory", inventory),
                new Route("/api/v1/telehealth", inventory),
                new Route("/api/v1/analytics", inventory),
                new Route("/api/v1/scheduling", inventory),
                new Route("/api/v1/population", inventory));
    }}

    public String targetFor(String uri) {{
        Route best = null;
        for (Route r : routes) {{
            if (uri.equals(r.prefix()) || uri.startsWith(r.prefix() + "/")) {{
                if (best == null || r.prefix().length() > best.prefix().length()) best = r;
            }}
        }}
        return best == null ? monolith : best.target();
    }}

    public String monolithUrl() {{ return monolith; }}
}}
""")

w(f"{G}/src/main/java/{PD}/filter/GatewayFilters.java", f"""package {P}.filter;

import com.healthcare.contracts.ApiError;
import com.healthcare.contracts.Correlation;
import com.fasterxml.jackson.databind.ObjectMapper;
import {P}.security.JwtUtil;
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
public class GatewayFilters extends OncePerRequestFilter {{
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/v1/auth/", "/actuator/health", "/actuator/info", "/gateway/health");
    private final JwtUtil jwt;
    private final ObjectMapper om;
    private final int limitPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public GatewayFilters(JwtUtil jwt, ObjectMapper om,
                          @Value("${{gw.rate-limit-per-minute:100}}") int limitPerMinute) {{
        this.jwt = jwt; this.om = om; this.limitPerMinute = limitPerMinute;
    }}

    static class Window {{
        final AtomicLong minute = new AtomicLong(System.currentTimeMillis() / 60000);
        final AtomicInteger count = new AtomicInteger();
    }}

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {{
        String uri = req.getRequestURI();
        String cid = Correlation.ensure(req.getHeader(Correlation.HEADER));
        MDC.put("correlationId", cid);
        MDC.put("service", "api-gateway");
        res.setHeader(Correlation.HEADER, cid);
        req.setAttribute(Correlation.HEADER, cid);
        logger.info("gateway " + req.getMethod() + " " + uri + " corr=" + cid);
        try {{
            // Rate limiting (per client IP, fixed window).
            String ip = req.getRemoteAddr();
            Window win = windows.computeIfAbsent(ip, k -> new Window());
            long now = System.currentTimeMillis() / 60000;
            if (win.minute.get() != now) {{ win.minute.set(now); win.count.set(0); }}
            if (win.count.incrementAndGet() > limitPerMinute) {{
                writeError(req, res, 429, "RATE_LIMITED", "Too many requests");
                return;
            }}
            // Authentication boundary.
            boolean pub = PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith)
                    || uri.equals("/api/v1/auth") || uri.equals("/api/v1/auth/login")
                    || uri.equals("/api/v1/auth/register") || uri.equals("/api/v1/auth/refresh");
            if (!pub && uri.startsWith("/api/")) {{
                String h = req.getHeader("Authorization");
                if (h == null || !h.startsWith("Bearer ")) {{
                    writeError(req, res, 401, "UNAUTHORIZED", "Missing bearer token");
                    return;
                }}
                String token = h.substring(7);
                if (!jwt.valid(token)) {{
                    writeError(req, res, 401, "INVALID_TOKEN", "Invalid or expired token");
                    return;
                }}
                Claims c = jwt.parse(token);
                req.setAttribute("username", c.getSubject());
                req.setAttribute("roles", jwt.roles(c));
                res.setHeader("X-User", c.getSubject());
                res.setHeader("X-Roles", String.join(",", jwt.roles(c)));
            }}
            chain.doFilter(req, res);
        }} finally {{
            MDC.clear();
        }}
    }}

    private void writeError(HttpServletRequest req, HttpServletResponse res, int status,
                            String code, String message) throws IOException {{
        res.setStatus(status);
        res.setContentType("application/json");
        Object cid = req.getAttribute(Correlation.HEADER);
        om.writeValue(res.getOutputStream(),
                new ApiError(status, code, message, cid == null ? null : String.valueOf(cid), "api-gateway"));
    }}
}}
""")

w(f"{G}/src/main/java/{PD}/proxy/ProxyController.java", f"""package {P}.proxy;

import com.healthcare.contracts.ApiError;
import com.healthcare.contracts.Correlation;
import {P}.routing.RouteTable;
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
public class ProxyController {{
    private static final Set<String> HOP_HEADERS = Set.of(
            "host", "content-length", "transfer-encoding", "connection");
    private final RouteTable routes;
    private final RestTemplate rest;

    public ProxyController(RouteTable routes, RestTemplateBuilder builder,
                           @Value("${{gw.timeout-ms:5000}}") int timeoutMs) {{
        this.routes = routes;
        this.rest = builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }}

    @RequestMapping("/api/**")
    public ResponseEntity<?> proxy(HttpServletRequest req) throws Exception {{
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String target = routes.targetFor(uri);
        String url = target + uri + (query == null ? "" : "?" + query);

        byte[] body = req.getInputStream().readAllBytes();
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {{
            String n = names.nextElement();
            if (HOP_HEADERS.contains(n.toLowerCase())) continue;
            headers.put(n, List.list(req.getHeader(n)));
        }}
        Object cid = req.getAttribute(Correlation.HEADER);
        if (cid != null) headers.set(Correlation.HEADER, String.valueOf(cid));

        HttpMethod method = HttpMethod.valueOf(req.getMethod());
        try {{
            ResponseEntity<byte[]> downstream =
                    rest.exchange(url, method, new HttpEntity<>(body, headers), byte[].class);
            HttpHeaders out = new HttpHeaders();
            String ct = downstream.getHeaders().getFirst("Content-Type");
            if (ct != null) out.set("Content-Type", ct);
            if (cid != null) out.set(Correlation.HEADER, String.valueOf(cid));
            return new ResponseEntity<>(downstream.getBody(), out, downstream.getStatusCode());
        }} catch (HttpStatusCodeException e) {{
            return ResponseEntity.status(e.getStatusCode())
                    .header("Content-Type", "application/json")
                    .body(e.getResponseBodyAsByteArray());
        }} catch (ResourceAccessException e) {{
            Object c = req.getAttribute(Correlation.HEADER);
            return ResponseEntity.status(503).body(new ApiError(503, "DOWNSTREAM_UNAVAILABLE",
                    "Downstream service unavailable: " + target,
                    c == null ? null : String.valueOf(c), "api-gateway"));
        }}
    }}

    @RequestMapping("/gateway/health")
    public Map<String, String> health() {{
        return Map.of("service", "api-gateway", "status", "UP");
    }}
}}
""")

w(f"{G}/src/main/java/{PD}/config/GatewayConfig.java", f"""package {P}.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class GatewayConfig {{
    @Bean
    public SecurityFilterChain chain(HttpSecurity http) throws Exception {{
        // JWT boundary is enforced in GatewayFilters; Spring Security stays permissive
        // but provides CORS + stateless + consistent 401/403 error surface.
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }}

    @Bean
    public CorsConfigurationSource corsSource() {{
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("X-Correlation-Id", "X-User", "X-Roles"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }}
}}
""")

w(f"{G}/src/test/resources/application.properties", """jwt.secret=REDACTED
gw.auth-url=http://localhost:8101
gw.patient-url=http://localhost:8102
gw.provider-url=http://localhost:8103
gw.appointment-url=http://localhost:8104
gw.medication-url=http://localhost:8105
gw.billing-url=http://localhost:8106
gw.ai-url=http://localhost:8107
gw.rag-url=http://localhost:8108
gw.cds-url=http://localhost:8109
gw.notification-url=http://localhost:8110
gw.inventory-url=http://localhost:8111
gw.monolith-url=http://localhost:8081
""")

w(f"{G}/src/main/java/{PD}/filter/package-info.java", f"""package {P}.filter;
""")

# gateway tests
w(f"{G}/src/test/java/{PD}/RouteTableTest.java", f"""package {P};

import {P}.routing.RouteTable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouteTableTest {{
    RouteTable table() {{
        return new RouteTable("A", "PAT", "PROV", "APPT", "MED", "BILL", "AI", "RAG",
                "CDS", "NOTIF", "INV", "MONO");
    }}

    @Test
    public void routesResolveToOwningService() {{
        RouteTable t = table();
        assertEquals("A", t.targetFor("/api/v1/auth/login"));
        assertEquals("PAT", t.targetFor("/api/v1/patients/1"));
        assertEquals("PROV", t.targetFor("/api/v1/providers/search"));
        assertEquals("APPT", t.targetFor("/api/v1/appointments"));
        assertEquals("APPT", t.targetFor("/api/v1/encounters/5"));
        assertEquals("MED", t.targetFor("/api/v1/medications/patient/1"));
        assertEquals("BILL", t.targetFor("/api/v1/billing/invoices"));
        assertEquals("BILL", t.targetFor("/api/v1/claims/2"));
        assertEquals("AI", t.targetFor("/api/v1/ai/triage"));
        assertEquals("AI", t.targetFor("/api/v1/triage/score"));
        assertEquals("RAG", t.targetFor("/api/v1/rag/search"));
        assertEquals("RAG", t.targetFor("/api/v1/kb/articles"));
        assertEquals("CDS", t.targetFor("/api/v1/cds/check-interactions"));
        assertEquals("NOTIF", t.targetFor("/api/v1/notifications"));
        assertEquals("INV", t.targetFor("/api/v1/inventory/items"));
        assertEquals("INV", t.targetFor("/api/v1/telehealth/sessions"));
    }}

    @Test
    public void unknownPathsFallBackToMonolith() {{
        assertEquals("MONO", table().targetFor("/api/v1/records/1"));
    }}
}}
""")

w(f"{G}/src/test/java/{PD}/GatewaySecurityTest.java", f"""package {P};

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GatewaySecurityTest {{
    @Autowired MockMvc mvc;
    @Value("${{jwt.secret}}") String secret;

    String token(boolean expired) {{
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        long exp = expired ? System.currentTimeMillis() - 1000 : System.currentTimeMillis() + 600000;
        return Jwts.builder().setSubject("u").claim("roles", List.of("PATIENT"))
                .setIssuedAt(new Date()).setExpiration(new Date(exp))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }}

    @Test
    public void protectedPathsRequireValidToken() throws Exception {{
        mvc.perform(get("/api/v1/patients/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/patients/1").header("Authorization", "Bearer bogus"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
        mvc.perform(get("/api/v1/patients/1").header("Authorization", "Bearer " + token(true)))
                .andExpect(status().isUnauthorized());
    }}

    @Test
    public void correlationIdIsPropagated() throws Exception {{
        mvc.perform(get("/gateway/health").header("X-Correlation-Id", "corr-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "corr-123"));
        mvc.perform(get("/gateway/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }}
}}
""")

w(f"{G}/Dockerfile", """FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/api-gateway.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
""")

# ---------------- docker-compose ----------------
w("docker-compose.yml", """# Phase 13 local microservices environment.
# Usage:
#   mvn -f platform/pom.xml package -DskipTests
#   docker compose -f platform/docker-compose.yml up --build
# Monolith (Phase 1-12) optionally runs on 8081 as fallback route target.
services:
  kafka:
    image: apache/kafka:3.8.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093,PLAINTEXT_HOST://:29092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      CLUSTER_ID: healthcare-platform-v1
    ports:
      - "9092:9092"
      - "29092:29092"
    healthcheck:
      test: ["CMD-SHELL", "cub kafka-ready -b localhost:9092 1 5 || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 10

  api-gateway:
    build: ./api-gateway
    ports: ["8080:8080"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      AUTH_URL: http://auth-service:8101
      PATIENT_URL: http://patient-service:8102
      PROVIDER_URL: http://provider-service:8103
      APPOINTMENT_URL: http://appointment-service:8104
      MEDICATION_URL: http://medication-service:8105
      BILLING_URL: http://billing-service:8106
      AI_URL: http://ai-service:8107
      RAG_URL: http://rag-service:8108
      CDS_URL: http://cds-service:8109
      NOTIFICATION_URL: http://notification-service:8110
      INVENTORY_URL: http://inventory-service:8111
      MONOLITH_URL: http://monolith:8080
    depends_on: [auth-service, patient-service, appointment-service]

  auth-service:
    build: ./auth-service
    ports: ["8101:8101"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  patient-service:
    build: ./patient-service
    ports: ["8102:8102"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  provider-service:
    build: ./provider-service
    ports: ["8103:8103"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  appointment-service:
    build: ./appointment-service
    ports: ["8104:8104"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
      PROVIDER_URL: http://provider-service:8103
      PATIENT_URL: http://patient-service:8102
    depends_on: {kafka: {condition: service_started}}
  medication-service:
    build: ./medication-service
    ports: ["8105:8105"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  billing-service:
    build: ./billing-service
    ports: ["8106:8106"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  ai-service:
    build: ./ai-service
    ports: ["8107:8107"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
      RAG_URL: http://rag-service:8108
      CDS_URL: http://cds-service:8109
    depends_on: {kafka: {condition: service_started}}
  rag-service:
    build: ./rag-service
    ports: ["8108:8108"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  cds-service:
    build: ./cds-service
    ports: ["8109:8109"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  notification-service:
    build: ./notification-service
    ports: ["8110:8110"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
  inventory-service:
    build: ./inventory-service
    ports: ["8111:8111"]
    environment:
      JWT_SECRET: ${JWT_SECRET:-REDACTED}
      KAFKA_BOOTSTRAP: kafka:9092
    depends_on: {kafka: {condition: service_started}}
""")

# ---------------- verify script ----------------
w("verify-phase13.ps1", """# Phase 13 live verification: gateway + services + E2E (requires built jars).
$ErrorActionPreference = "Continue"
function Check($name, $url, $method = "GET", $body = $null, $token = $null, $expect = 200) {
    try {
        $h = @{}
        if ($token) { $h["Authorization"] = "Bearer $token" }
        $h["X-Correlation-Id"] = "verify-$([guid]::NewGuid().ToString().Substring(0,8))"
        $params = @{Uri = $url; Method = $method; Headers = $h; TimeoutSec = 20}
        if ($body) { $params["Body"] = $body; $params["ContentType"] = "application/json" }
        $r = Invoke-WebRequest @params -UseBasicParsing
        if ($r.StatusCode -eq $expect) { Write-Host "PASS $name [$($r.StatusCode)]"; return $r }
        else { Write-Host "FAIL $name [got $($r.StatusCode), want $expect]"; return $null }
    } catch {
        $code = $_.Exception.Response.StatusCode.Value__ 2>$null
        if ($code -eq $expect) { Write-Host "PASS $name [$code]" }
        else { Write-Host "FAIL $name [$($_.Exception.Message)]" }
        return $null
    }
}
$GW = "http://localhost:8080"
Write-Host "=== gateway health ==="
Check "gateway-health" "$GW/gateway/health"
Write-Host "=== security: no token must be 401 ==="
Check "no-token-401" "$GW/api/v1/patients/1" "GET" $null $null 401
Write-Host "=== auth: register + login ==="
$reg = Check "register" "$GW/api/v1/auth/register" "POST" '{"username":"phase13user","password":"password123","role":"PATIENT"}' $null 201
$login = Check "login" "$GW/api/v1/auth/login" "POST" '{"username":"phase13user","password":"password123"}' $null 200
if ($login) {
    $tok = ($login.Content | ConvertFrom-Json).accessToken
    Write-Host "=== patient via gateway ==="
    $p = Check "patient-create" "$GW/api/v1/patients" "POST" '{"firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1990-01-01","gender":"F"}' $tok 201
    Write-Host "=== provider via gateway ==="
    Check "provider-search" "$GW/api/v1/providers/search?specialty=cardio" "GET" $null $tok 200
    Write-Host "=== ai triage guardrails via gateway ==="
    Check "ai-triage" "$GW/api/v1/ai/triage" "POST" '{"symptoms":"I have chest pain"}' $tok 200
    Write-Host "=== cds via gateway (patient role must be 403) ==="
    Check "cds-forbidden" "$GW/api/v1/cds/check-interactions" "POST" '{"drugs":["warfarin","aspirin"]}' $tok 403
    Write-Host "=== rag via gateway ==="
    Check "rag-search" "$GW/api/v1/rag/search?q=health" "GET" $null $tok 200
}
Write-Host "=== done ==="
""")

# ---------------- runbook ----------------
w("RUNBOOK.md", """# Phase 13 Microservices — Local Runbook

## Layout
- `api-gateway/` — single external entry point (port 8080)
- `auth-service/` (8101), `patient-service/` (8102), `provider-service/` (8103),
  `appointment-service/` (8104), `medication-service/` (8105), `billing-service/` (8106),
  `ai-service/` (8107), `rag-service/` (8108), `cds-service/` (8109),
  `notification-service/` (8110), `inventory-service/` (8111)
- `shared-contracts/` — DomainEvent envelope, ApiError, correlation + topic names
- Monolith (repo root, Phase 1-12) is preserved and reachable as gateway fallback.

## Database-per-service
Each service uses its own isolated H2 database (`./data/<service>-db` locally;
separate filesystem/volume per container in Docker). No service has entities or
datasources belonging to another service — ownership is enforced by packaging:
each service module only contains its own `entity/` + `repo/` packages.

## Build
```
.\\mvnw.cmd -f platform/pom.xml package -DskipTests
.\\mvnw.cmd -f platform/pom.xml test        # unit + integration (incl. embedded-Kafka)
```

## Run locally (no Docker)
Start services in order (each on its own port), then the gateway:
```
java -jar platform\\auth-service\\target\\auth-service.jar
java -jar platform\\patient-service\\target\\patient-service.jar
... (all services)
java -jar platform\\api-gateway\\target\\api-gateway.jar
```
Kafka is optional at runtime: producers/consumers tolerate a missing broker
(events are skipped with a warning) so the REST + gateway surface works without it.
With Docker, Kafka (KRaft) is provided — see below.

## Run with Docker
```
mvn -f platform/pom.xml package -DskipTests
docker compose -f platform/docker-compose.yml up --build
```
Inside compose, services address each other by service name
(`http://patient-service:8102`, `kafka:9092`). No localhost hardcoding:
every downstream URL is an env var (`*_URL`, `KAFKA_BOOTSTRAP`).

## Verify live
```
powershell -File platform\\verify-phase13.ps1
```
Covers: gateway health, 401 without token, register/login, patient create via
gateway, provider search, AI triage guardrails, CDS RBAC, RAG search.

## E2E flow
Client -> Gateway (JWT + correlation) -> Domain service -> own DB -> Kafka event
-> notification-service consumer -> stored notification (idempotent).
""")

print("part6 done")
