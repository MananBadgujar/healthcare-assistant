"""Phase 13 scaffolder part 2: common service template files + gateway."""
import os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "platform")

def w(path, content):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)

def java_pkg(svc):
    return "com.healthcare." + svc.replace("-", "")

def common_files(svc, port, extra_props=""):
    P = java_pkg(svc)
    base = svc
    # Application
    w(f"{base}/src/main/java/{P.replace('.','/')}/Application.java",
f"""package {P};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class Application {{
    public static void main(String[] args) {{
        SpringApplication.run(Application.class, args);
    }}
}}
""")
    # application.properties
    w(f"{base}/src/main/resources/application.properties",
f"""server.port={port}
spring.application.name={base}
spring.datasource.url=jdbc:h2:file:./data/{base}-db;MODE=MySQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
jwt.secret=${{JWT_SECRET:REDACTED}}
spring.kafka.bootstrap-servers=${{KAFKA_BOOTSTRAP:localhost:9092}}
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.consumer.group-id={base}-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.listener.missing-topics-fatal=false
service.name={base}
{extra_props}""")
    # JwtUtil
    w(f"{base}/src/main/java/{P.replace('.','/')}/security/JwtUtil.java",
f"""package {P}.security;

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
        if (secret == null || secret.isBlank()) throw new IllegalStateException("jwt.secret missing");
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
    # JwtAuthFilter
    w(f"{base}/src/main/java/{P.replace('.','/')}/security/JwtAuthFilter.java",
f"""package {P}.security;

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
public class JwtAuthFilter extends OncePerRequestFilter {{
    private final JwtUtil jwt;
    public JwtAuthFilter(JwtUtil jwt) {{ this.jwt = jwt; }}

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {{
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {{
            String token = h.substring(7);
            try {{
                if (jwt.valid(token)) {{
                    Claims c = jwt.parse(token);
                    List<SimpleGrantedAuthority> auths = jwt.roles(c).stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                    UsernamePasswordAuthenticationToken at =
                            new UsernamePasswordAuthenticationToken(c.getSubject(), null, auths);
                    at.setDetails(c);
                    SecurityContextHolder.getContext().setAuthentication(at);
                    req.setAttribute("username", c.getSubject());
                    req.setAttribute("roles", jwt.roles(c));
                }}
            }} catch (Exception ignored) {{
                SecurityContextHolder.clearContext();
            }}
        }}
        chain.doFilter(req, res);
    }}
}}
""")
    # SecurityConfig
    w(f"{base}/src/main/java/{P.replace('.','/')}/config/SecurityConfig.java",
f"""package {P}.config;

import {P}.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {{
    private final JwtAuthFilter filter;
    public SecurityConfig(JwtAuthFilter filter) {{ this.filter = filter; }}

    @Bean
    public SecurityFilterChain chain(HttpSecurity http) throws Exception {{
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }}

    @Bean
    public PasswordEncoder passwordEncoder() {{ return new BCryptPasswordEncoder(); }}
}}
""")
    # CorrelationFilter
    w(f"{base}/src/main/java/{P.replace('.','/')}/common/CorrelationFilter.java",
f"""package {P}.common;

import com.healthcare.contracts.Correlation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class CorrelationFilter extends OncePerRequestFilter {{
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {{
        String cid = Correlation.ensure(req.getHeader(Correlation.HEADER));
        MDC.put("correlationId", cid);
        MDC.put("service", "{base}");
        res.setHeader(Correlation.HEADER, cid);
        req.setAttribute(Correlation.HEADER, cid);
        try {{
            chain.doFilter(req, res);
        }} finally {{
            MDC.clear();
        }}
    }}
}}
""")
    # ApiError handler
    w(f"{base}/src/main/java/{P.replace('.','/')}/common/GlobalExceptionHandler.java",
f"""package {P}.common;

import com.healthcare.contracts.ApiError;
import com.healthcare.contracts.Correlation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {{
    private String cid(HttpServletRequest req) {{
        Object v = req.getAttribute(Correlation.HEADER);
        return v == null ? null : String.valueOf(v);
    }}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest req) {{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(400, "VALIDATION_FAILED", "Request validation failed", cid(req), "{base}"));
    }}

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException e, HttpServletRequest req) {{
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(403, "FORBIDDEN", "Insufficient permissions", cid(req), "{base}"));
    }}

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> notFound(NoSuchElementException e, HttpServletRequest req) {{
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "NOT_FOUND", e.getMessage(), cid(req), "{base}"));
    }}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException e, HttpServletRequest req) {{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(400, "BUSINESS_RULE_VIOLATION", e.getMessage(), cid(req), "{base}"));
    }}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest req) {{
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "INTERNAL_ERROR", "Unexpected server error", cid(req), "{base}"));
    }}
}}
""")
    # AuditService
    w(f"{base}/src/main/java/{P.replace('.','/')}/common/AuditService.java",
f"""package {P}.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/** Structured audit logging foundation (Phase 15 will ship to a sink). */
@Component
public class AuditService {{
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public void record(String user, String action, String entity, String entityId, String result) {{
        log.info("audit user={{}} service={base} action={{}} entity={{}} entityId={{}} correlationId={{}} result={{}}",
                user, action, entity, entityId, MDC.get("correlationId"), result);
    }}
}}
""")
    # ResilienceUtil: timeout/resttemplate + simple circuit breaker
    w(f"{base}/src/main/java/{P.replace('.','/')}/common/ServiceClients.java",
f"""package {P}.common;

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
public class ServiceClients {{
    private final RestTemplate rest;
    private final Map<String, Breaker> breakers = new ConcurrentHashMap<>();

    public ServiceClients(RestTemplateBuilder builder) {{
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }}

    public ResponseEntity<String> get(String baseUrl, String path, String authHeader) {{
        return call(baseUrl, path, HttpMethod.GET, null, authHeader);
    }}

    public ResponseEntity<String> call(String baseUrl, String path, HttpMethod method,
                                       String body, String authHeader) {{
        Breaker b = breakers.computeIfAbsent(baseUrl, k -> new Breaker());
        if (!b.allow()) throw new ResourceAccessException("Circuit OPEN for " + baseUrl);
        HttpHeaders h = new HttpHeaders();
        h.set("Accept", "application/json");
        h.set("Content-Type", "application/json");
        String cid = MDC.get("correlationId");
        if (cid != null) h.set(Correlation.HEADER, cid);
        if (authHeader != null) h.set("Authorization", authHeader);
        try {{
            ResponseEntity<String> r = rest.exchange(baseUrl + path, method,
                    new HttpEntity<>(body, h), String.class);
            b.success();
            return r;
        }} catch (Exception e) {{
            b.failure();
            throw e;
        }}
    }}

    static class Breaker {{
        private final AtomicInteger failures = new AtomicInteger();
        private volatile long openedAt;
        boolean allow() {{
            if (openedAt == 0) return true;
            if (System.currentTimeMillis() - openedAt > 30_000) {{ openedAt = 0; failures.set(0); return true; }}
            return false;
        }}
        void success() {{ failures.set(0); openedAt = 0; }}
        void failure() {{ if (failures.incrementAndGet() >= 5) openedAt = System.currentTimeMillis(); }}
    }}
}}
""")
    # EventPublisher
    w(f"{base}/src/main/java/{P.replace('.','/')}/common/EventPublisher.java",
f"""package {P}.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Reliable-enough dev publisher: DB commit happens first, then publish; consumer is idempotent. */
@Component
public class EventPublisher {{
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om;

    public EventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper om) {{
        this.kafka = kafka;
        this.om = om;
    }}

    public void publish(String topic, DomainEvent event) {{
        try {{
            if (event.getCorrelationId() == null) event.setCorrelationId(MDC.get("correlationId"));
            kafka.send(topic, event.getEntityId(), om.writeValueAsString(event));
        }} catch (Exception e) {{
            throw new IllegalStateException("Failed to publish event " + event.getEventName(), e);
        }}
    }}
}}
""")
    # HealthController
    w(f"{base}/src/main/java/{P.replace('.','/')}/health/HealthController.java",
f"""package {P}.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {{
    @GetMapping("/api/health")
    public Map<String, String> health() {{
        return Map.of("service", "{base}", "status", "UP");
    }}
}}
""")
    # test props + Dockerfile
    w(f"{base}/src/test/resources/application.properties",
"""jwt.secret=REDACTED
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers:localhost:9092}
spring.kafka.listener.missing-topics-fatal=false
""")
    w(f"{base}/Dockerfile",
f"""FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/{base}.jar app.jar
EXPOSE {port}
ENTRYPOINT ["java","-jar","/app/app.jar"]
""")

print("part2 done")
