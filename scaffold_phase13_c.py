"""Phase 13 scaffolder part 3: domain slices for all 11 services."""
import os
from scaffold_phase13_b import w, common_files, java_pkg
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "platform")

def J(svc, *parts):
    P = java_pkg(svc).replace(".", "/")
    return f"{svc}/src/main/java/{P}/" + "/".join(parts)

def T(svc, *parts):
    P = java_pkg(svc).replace(".", "/")
    return f"{svc}/src/test/java/{P}/" + "/".join(parts)

# ============================ AUTH SERVICE (8101) ============================
SVC = "auth-service"; common_files(SVC, 8101)
P = java_pkg(SVC)
w(J(SVC, "entity/AppUser.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class AppUser {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String role;
    private String mfaSecret;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public String getUsername() {{ return username; }} public void setUsername(String u) {{ this.username = u; }}
    public String getPasswordHash() {{ return passwordHash; }} public void setPasswordHash(String p) {{ this.passwordHash = p; }}
    public String getRole() {{ return role; }} public void setRole(String r) {{ this.role = r; }}
    public String getMfaSecret() {{ return mfaSecret; }} public void setMfaSecret(String m) {{ this.mfaSecret = m; }}
}}
""")
w(J(SVC, "repo/UserRepository.java"), f"""package {P}.repo;

import {P}.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {{
    Optional<AppUser> findByUsername(String username);
}}
""")
w(J(SVC, "service/AuthService.java"), f"""package {P}.service;

import {P}.entity.AppUser;
import {P}.repo.UserRepository;
import {P}.security.JwtIssuer;
import {P}.common.AuditService;
import {P}.common.EventPublisher;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {{
    private static final Set<String> ROLES = Set.of("ADMIN", "PROVIDER", "PATIENT", "STAFF");
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtIssuer issuer;
    private final AuditService audit;
    private final EventPublisher events;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtIssuer issuer,
                       AuditService audit, EventPublisher events) {{
        this.users = users; this.encoder = encoder; this.issuer = issuer;
        this.audit = audit; this.events = events;
    }}

    @Transactional
    public AppUser register(String username, String password, String role) {{
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password must be >= 8 chars");
        String r = (role == null) ? "PATIENT" : role.toUpperCase();
        if (!ROLES.contains(r)) throw new IllegalArgumentException("Unknown role: " + role);
        if (users.findByUsername(username).isPresent()) throw new IllegalArgumentException("Username already exists");
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(r);
        AppUser saved = users.save(u);
        audit.record(username, "REGISTER", "User", String.valueOf(saved.getId()), "SUCCESS");
        try {{
            events.publish("healthcare.auth.events.registered",
                DomainEvent.of("auth.user.registered", "User", String.valueOf(saved.getId()),
                    "auth-service", MDC.get("correlationId"),
                    Map.of("username", username, "role", r)));
        }} catch (Exception ignored) {{}}
        return saved;
    }}

    public Map<String, String> login(String username, String password) {{
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(password, u.getPasswordHash())) {{
            audit.record(username, "LOGIN", "User", String.valueOf(u.getId()), "FAILED");
            throw new IllegalArgumentException("Invalid credentials");
        }}
        audit.record(username, "LOGIN", "User", String.valueOf(u.getId()), "SUCCESS");
        String access = issuer.access(u.getUsername(), List.of(u.getRole()));
        String refresh = issuer.refresh(u.getUsername());
        return Map.of("accessToken", access, "refreshToken", refresh, "role", u.getRole());
    }}

    public Map<String, String> refresh(String refreshToken, JwtIssuer issuer) {{
        String username = issuer.subjectIfValid(refreshToken);
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        return Map.of("accessToken", issuer.access(u.getUsername(), List.of(u.getRole())));
    }}
}}
""")
w(J(SVC, "security/JwtIssuer.java"), f"""package {P}.security;

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
public class JwtIssuer {{
    private final Key key;
    public JwtIssuer(@Value("${{jwt.secret}}") String secret) {{
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }}
    public String access(String username, List<String> roles) {{
        return Jwts.builder().setSubject(username).claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }}
    public String refresh(String username) {{
        return Jwts.builder().setSubject(username).claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 604_800_000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }}
    public String subjectIfValid(String token) {{
        try {{
            var c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            if (c.getExpiration() != null && c.getExpiration().before(new Date()))
                throw new IllegalArgumentException("Refresh token expired");
            return c.getSubject();
        }} catch (Exception e) {{
            throw new IllegalArgumentException("Invalid refresh token");
        }}
    }}
}}
""")
w(J(SVC, "web/AuthController.java"), f"""package {P}.web;

import {P}.entity.AppUser;
import {P}.security.JwtIssuer;
import {P}.service.AuthService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {{
    private final AuthService auth;
    private final JwtIssuer issuer;
    public AuthController(AuthService auth, JwtIssuer issuer) {{ this.auth = auth; this.issuer = issuer; }}

    public record RegisterReq(@NotBlank String username, @NotBlank String password, String role) {{}}
    public record LoginReq(@NotBlank String username, @NotBlank String password) {{}}
    public record RefreshReq(@NotBlank String refreshToken) {{}}

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterReq req) {{
        AppUser u = auth.register(req.username(), req.password(), req.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole()));
    }}

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginReq req) {{
        return ResponseEntity.ok(auth.login(req.username(), req.password()));
    }}

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshReq req) {{
        return ResponseEntity.ok(auth.refresh(req.refreshToken(), issuer));
    }}

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {{
        if (authentication == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("username", authentication.getName(),
                "roles", authentication.getAuthorities().stream().map(Object::toString).toList()));
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic authRegistered() {{
        return new NewTopic("healthcare.auth.events.registered", 1, (short) 1);
    }}
}}
""")
w(T(SVC, "AuthFlowTest.java"), f"""package {P};

import {P}.security.JwtIssuer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthFlowTest {{
    @Autowired MockMvc mvc;

    @Test
    public void registerLoginAndMe() throws Exception {{
        String body = "{{\\"username\\":\\"alice\\",\\"password\\":\\"password123\\",\\"role\\":\\"PATIENT\\"}}";
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        String login = "{{\\"username\\":\\"alice\\",\\"password\\":\\"password123\\"}}";
        String resp = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = resp.split("\\"accessToken\\":\\"")[1].split("\\"")[0];
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }}

    @Test
    public void badCredentialsRejected() throws Exception {{
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"username\\":\\"ghost\\",\\"password\\":\\"nope12345\\"}}"))
                .andExpect(status().is4xxClientError());
    }}
}}
""")

# ============================ PATIENT SERVICE (8102) ============================
SVC = "patient-service"; common_files(SVC, 8102)
P = java_pkg(SVC)
w(J(SVC, "entity/Patient.java"), f"""package {P}.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
public class Patient {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String ownerUsername;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public String getFirstName() {{ return firstName; }} public void setFirstName(String v) {{ this.firstName = v; }}
    public String getLastName() {{ return lastName; }} public void setLastName(String v) {{ this.lastName = v; }}
    public LocalDate getDateOfBirth() {{ return dateOfBirth; }} public void setDateOfBirth(LocalDate v) {{ this.dateOfBirth = v; }}
    public String getGender() {{ return gender; }} public void setGender(String v) {{ this.gender = v; }}
    public String getOwnerUsername() {{ return ownerUsername; }} public void setOwnerUsername(String v) {{ this.ownerUsername = v; }}
}}
""")
w(J(SVC, "repo/PatientRepository.java"), f"""package {P}.repo;

import {P}.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {{
}}
""")
w(J(SVC, "service/PatientService.java"), f"""package {P}.service;

import {P}.common.AuditService;
import {P}.common.EventPublisher;
import {P}.entity.Patient;
import {P}.repo.PatientRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PatientService {{
    private final PatientRepository repo;
    private final AuditService audit;
    private final EventPublisher events;

    public PatientService(PatientRepository repo, AuditService audit, EventPublisher events) {{
        this.repo = repo; this.audit = audit; this.events = events;
    }}

    @Transactional
    public Patient create(Patient p, String owner) {{
        if (p.getFirstName() == null || p.getFirstName().isBlank()) throw new IllegalArgumentException("firstName required");
        if (p.getLastName() == null || p.getLastName().isBlank()) throw new IllegalArgumentException("lastName required");
        p.setId(null);
        p.setOwnerUsername(owner);
        Patient saved = repo.save(p);
        audit.record(owner, "CREATE", "Patient", String.valueOf(saved.getId()), "SUCCESS");
        try {{
            events.publish(Topics.PATIENT_CREATED, DomainEvent.of("patient.created", "Patient",
                    String.valueOf(saved.getId()), "patient-service", MDC.get("correlationId"),
                    Map.of("owner", owner == null ? "" : owner)));
        }} catch (Exception ignored) {{}}
        return saved;
    }}

    public Patient get(Long id, String requester, boolean privileged) {{
        Patient p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Patient not found"));
        if (!privileged && (p.getOwnerUsername() == null || !p.getOwnerUsername().equals(requester)))
            throw new org.springframework.security.access.AccessDeniedException("Not your patient record");
        return p;
    }}

    @Transactional
    public Patient update(Long id, Patient patch) {{
        Patient p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Patient not found"));
        if (patch.getFirstName() != null) p.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) p.setLastName(patch.getLastName());
        if (patch.getDateOfBirth() != null) p.setDateOfBirth(patch.getDateOfBirth());
        if (patch.getGender() != null) p.setGender(patch.getGender());
        Patient saved = repo.save(p);
        try {{
            events.publish(Topics.PATIENT_UPDATED, DomainEvent.of("patient.updated", "Patient",
                    String.valueOf(saved.getId()), "patient-service", MDC.get("correlationId"), Map.of()));
        }} catch (Exception ignored) {{}}
        return saved;
    }}
}}
""")
w(J(SVC, "web/PatientController.java"), f"""package {P}.web;

import {P}.entity.Patient;
import {P}.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {{
    private final PatientService service;
    public PatientController(PatientService service) {{ this.service = service; }}

    private boolean privileged(Authentication a) {{
        return a.getAuthorities().stream().anyMatch(g ->
                g.getAuthority().equals("ROLE_ADMIN") || g.getAuthority().equals("ROLE_PROVIDER")
                        || g.getAuthority().equals("ROLE_STAFF"));
    }}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Patient> create(@RequestBody Patient p, Authentication auth) {{
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p, auth.getName()));
    }}

    @GetMapping("/{{id}}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Patient> get(@PathVariable Long id, Authentication auth) {{
        return ResponseEntity.ok(service.get(id, auth.getName(), privileged(auth)));
    }}

    @PatchMapping("/{{id}}/update")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public ResponseEntity<Patient> update(@PathVariable Long id, @RequestBody Patient patch) {{
        return ResponseEntity.ok(service.update(id, patch));
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic patientCreated() {{ return new NewTopic("healthcare.patient.events.created", 1, (short) 1); }}
    @Bean public NewTopic patientUpdated() {{ return new NewTopic("healthcare.patient.events.updated", 1, (short) 1); }}
}}
""")
w(T(SVC, "PatientApiTest.java"), f"""package {P};

import {P}.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientApiTest {{
    @Autowired MockMvc mvc;
    @Value("${{jwt.secret}}") String secret;

    String token(String user, String... roles) {{
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }}

    @Test
    public void crudAndOwnership() throws Exception {{
        String admin = token("admin1", "ADMIN");
        String body = "{{\\"firstName\\":\\"Jane\\",\\"lastName\\":\\"Doe\\",\\"dateOfBirth\\":\\"1990-01-01\\",\\"gender\\":\\"F\\"}}";
        String resp = mvc.perform(post("/api/v1/patients").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\\"id\\":")[1].split("[,}}]")[0].trim();
        mvc.perform(get("/api/v1/patients/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/patients/" + id)).andExpect(status().isForbidden());
        String other = token("stranger", "PATIENT");
        mvc.perform(get("/api/v1/patients/" + id).header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/patients/999999").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }}
}}
""")

print("part3a done")
