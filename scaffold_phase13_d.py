"""Phase 13 scaffolder part 4: provider, appointment, medication, billing."""
import os
from scaffold_phase13_b import w, common_files, java_pkg
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "platform")

def J(svc, *parts):
    P = java_pkg(svc).replace(".", "/")
    return f"{svc}/src/main/java/{P}/" + "/".join(parts)

def T(svc, *parts):
    P = java_pkg(svc).replace(".", "/")
    return f"{svc}/src/test/java/{P}/" + "/".join(parts)

TOKEN_HELPER = """    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
"""

# ============================ PROVIDER SERVICE (8103) ============================
SVC = "provider-service"; common_files(SVC, 8103)
P = java_pkg(SVC)
w(J(SVC, "entity/Provider.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "providers")
public class Provider {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String specialty;
    private String facility;
    @Column(length = 2000)
    private String availableSlots;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public String getName() {{ return name; }} public void setName(String v) {{ this.name = v; }}
    public String getSpecialty() {{ return specialty; }} public void setSpecialty(String v) {{ this.specialty = v; }}
    public String getFacility() {{ return facility; }} public void setFacility(String v) {{ this.facility = v; }}
    public String getAvailableSlots() {{ return availableSlots; }} public void setAvailableSlots(String v) {{ this.availableSlots = v; }}
}}
""")
w(J(SVC, "repo/ProviderRepository.java"), f"""package {P}.repo;

import {P}.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProviderRepository extends JpaRepository<Provider, Long> {{
    List<Provider> findBySpecialtyContainingIgnoreCase(String specialty);
}}
""")
w(J(SVC, "web/ProviderController.java"), f"""package {P}.web;

import {P}.entity.Provider;
import {P}.repo.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {{
    private final ProviderRepository repo;
    public ProviderController(ProviderRepository repo) {{ this.repo = repo; }}

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public List<Provider> search(@RequestParam(required = false) String specialty) {{
        if (specialty == null || specialty.isBlank()) return repo.findAll();
        return repo.findBySpecialtyContainingIgnoreCase(specialty);
    }}

    @GetMapping("/{{id}}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Provider get(@PathVariable Long id) {{
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Provider not found"));
    }}

    @GetMapping("/{{id}}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<?> availability(@PathVariable Long id) {{
        Provider p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Provider not found"));
        return ResponseEntity.ok(java.util.Map.of("providerId", p.getId(), "availableSlots",
                p.getAvailableSlots() == null ? "" : p.getAvailableSlots()));
    }}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Provider> create(@RequestBody Provider p) {{
        if (p.getName() == null || p.getName().isBlank()) throw new IllegalArgumentException("name required");
        p.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(p));
    }}
}}
""")
w(T(SVC, "ProviderApiTest.java"), f"""package {P};

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProviderApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void searchAndAvailability() throws Exception {{
        String admin = token("admin", "ADMIN");
        String body = "{{\\"name\\":\\"Dr Smith\\",\\"specialty\\":\\"Cardiology\\",\\"facility\\":\\"Central\\",\\"availableSlots\\":\\"2026-09-20T09:00\\"}}";
        String resp = mvc.perform(post("/api/v1/providers").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\\"id\\":")[1].split("[,}}]")[0].trim();
        String patient = token("pat", "PATIENT");
        mvc.perform(get("/api/v1/providers/search?specialty=cardio").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/providers/" + id + "/availability").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/providers/search")).andExpect(status().isForbidden());
    }}
}}
""")

# ============================ APPOINTMENT SERVICE (8104) ============================
SVC = "appointment-service"
common_files(SVC, 8104, "provider.service.base-url=${PROVIDER_URL:http://localhost:8103}\npatient.service.base-url=${PATIENT_URL:http://localhost:8102}\n")
P = java_pkg(SVC)
w(J(SVC, "entity/Appointment.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "appointments")
public class Appointment {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private Long providerId;
    private String startTime;
    private String status = "PENDING";
    @Column(unique = true)
    private String idempotencyKey;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public Long getPatientId() {{ return patientId; }} public void setPatientId(Long v) {{ this.patientId = v; }}
    public Long getProviderId() {{ return providerId; }} public void setProviderId(Long v) {{ this.providerId = v; }}
    public String getStartTime() {{ return startTime; }} public void setStartTime(String v) {{ this.startTime = v; }}
    public String getStatus() {{ return status; }} public void setStatus(String v) {{ this.status = v; }}
    public String getIdempotencyKey() {{ return idempotencyKey; }} public void setIdempotencyKey(String v) {{ this.idempotencyKey = v; }}
}}
""")
w(J(SVC, "repo/AppointmentRepository.java"), f"""package {P}.repo;

import {P}.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {{
    Optional<Appointment> findByIdempotencyKey(String key);
}}
""")
w(J(SVC, "service/AppointmentService.java"), f"""package {P}.service;

import {P}.common.AuditService;
import {P}.common.EventPublisher;
import {P}.common.ServiceClients;
import {P}.entity.Appointment;
import {P}.repo.AppointmentRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class AppointmentService {{
    private static final Set<String> TRANSITIONS = Set.of(
            "PENDING>CONFIRMED", "PENDING>CANCELLED", "CONFIRMED>CANCELLED",
            "CONFIRMED>COMPLETED", "PENDING>COMPLETED");
    private final AppointmentRepository repo;
    private final AuditService audit;
    private final EventPublisher events;
    private final ServiceClients clients;
    private final String providerBaseUrl;

    public AppointmentService(AppointmentRepository repo, AuditService audit, EventPublisher events,
                              ServiceClients clients,
                              @Value("${{provider.service.base-url}}") String providerBaseUrl) {{
        this.repo = repo; this.audit = audit; this.events = events;
        this.clients = clients; this.providerBaseUrl = providerBaseUrl;
    }}

    @Transactional
    public Appointment book(Appointment a, String requester, String authHeader, String idemKey) {{
        if (a.getProviderId() == null) throw new IllegalArgumentException("providerId required");
        if (a.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        if (idemKey != null && !idemKey.isBlank()) {{
            var existing = repo.findByIdempotencyKey(idemKey);
            if (existing.isPresent()) return existing.get();
        }}
        // Controlled synchronous validation: provider must exist (timeout + circuit breaker inside).
        try {{
            clients.get(providerBaseUrl, "/api/v1/providers/" + a.getProviderId(), authHeader);
        }} catch (Exception e) {{
            throw new IllegalArgumentException("Provider validation failed: unavailable or not found");
        }}
        a.setId(null);
        a.setStatus("PENDING");
        a.setIdempotencyKey(idemKey);
        Appointment saved = repo.save(a);
        audit.record(requester, "BOOK", "Appointment", String.valueOf(saved.getId()), "SUCCESS");
        try {{
            events.publish(Topics.APPOINTMENT_CREATED, DomainEvent.of("appointment.created", "Appointment",
                    String.valueOf(saved.getId()), "appointment-service", MDC.get("correlationId"),
                    Map.of("patientId", String.valueOf(saved.getPatientId()),
                            "providerId", String.valueOf(saved.getProviderId()),
                            "status", saved.getStatus())));
        }} catch (Exception ignored) {{}}
        return saved;
    }}

    @Transactional
    public Appointment changeStatus(Long id, String next, String requester) {{
        Appointment a = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Appointment not found"));
        String n = next.toUpperCase();
        if (!TRANSITIONS.contains(a.getStatus() + ">" + n))
            throw new IllegalArgumentException("Illegal transition " + a.getStatus() + " -> " + n);
        a.setStatus(n);
        Appointment saved = repo.save(a);
        audit.record(requester, "STATUS_" + n, "Appointment", String.valueOf(id), "SUCCESS");
        try {{
            String topic = n.equals("CANCELLED") ? Topics.APPOINTMENT_CANCELLED : Topics.APPOINTMENT_STATUS;
            events.publish(topic, DomainEvent.of("appointment." + n.toLowerCase(), "Appointment",
                    String.valueOf(id), "appointment-service", MDC.get("correlationId"),
                    Map.of("status", n)));
        }} catch (Exception ignored) {{}}
        return saved;
    }}
}}
""")
w(J(SVC, "web/AppointmentController.java"), f"""package {P}.web;

import {P}.entity.Appointment;
import {P}.repo.AppointmentRepository;
import {P}.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {{
    private final AppointmentService service;
    private final AppointmentRepository repo;
    public AppointmentController(AppointmentService service, AppointmentRepository repo) {{
        this.service = service; this.repo = repo;
    }}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Appointment> book(@RequestBody Appointment a, Authentication auth,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {{
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.book(a, auth.getName(), authHeader, idemKey));
    }}

    @GetMapping("/{{id}}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Appointment get(@PathVariable Long id) {{
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Appointment not found"));
    }}

    @PatchMapping("/{{id}}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Appointment status(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {{
        return service.changeStatus(id, body.get("status"), auth.getName());
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic apptCreated() {{ return new NewTopic("healthcare.appointment.events.created", 1, (short) 1); }}
    @Bean public NewTopic apptCancelled() {{ return new NewTopic("healthcare.appointment.events.cancelled", 1, (short) 1); }}
    @Bean public NewTopic apptStatus() {{ return new NewTopic("healthcare.appointment.events.status-changed", 1, (short) 1); }}
}}
""")
w(T(SVC, "AppointmentKafkaTest.java"), f"""package {P};

import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {{Topics.APPOINTMENT_CREATED}})
public class AppointmentKafkaTest {{
    static final BlockingQueue<String> RECEIVED = new ArrayBlockingQueue<>(10);

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ObjectMapper om;

    @KafkaListener(topics = Topics.APPOINTMENT_CREATED, groupId = "phase13-test")
    public void onEvent(ConsumerRecord<String, String> rec) {{
        RECEIVED.offer(rec.value());
    }}

    @Test
    public void producerDeliversToConsumer() throws Exception {{
        DomainEvent e = DomainEvent.of("appointment.created", "Appointment", "7",
                "appointment-service", "corr-test", Map.of("status", "PENDING"));
        kafka.send(Topics.APPOINTMENT_CREATED, "7", om.writeValueAsString(e));
        String raw = RECEIVED.poll(15, TimeUnit.SECONDS);
        assertNotNull(raw, "consumer should receive the event");
        DomainEvent back = om.readValue(raw, DomainEvent.class);
        assertEquals("7", back.getEntityId());
        assertEquals("corr-test", back.getCorrelationId());
    }}
}}
""")
w(T(SVC, "AppointmentApiTest.java"), f"""package {P};

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import {P}.common.ServiceClients;
import java.util.Date;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentApiTest {{
    @Autowired MockMvc mvc;
    @MockBean ServiceClients clients;
{TOKEN_HELPER}
    @Test
    public void bookAndTransition() throws Exception {{
        when(clients.get(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok("{{}}"));
        String staff = token("s", "STAFF");
        String resp = mvc.perform(post("/api/v1/appointments").header("Authorization", "Bearer " + staff)
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"patientId\\":1,\\"providerId\\":2,\\"startTime\\":\\"2026-09-20T10:00\\"}}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\\"id\\":")[1].split("[,}}]")[0].trim();
        // duplicate idempotency key returns same record
        mvc.perform(post("/api/v1/appointments").header("Authorization", "Bearer " + staff)
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"patientId\\":1,\\"providerId\\":2,\\"startTime\\":\\"2026-09-20T10:00\\"}}"))
                .andExpect(status().isCreated());
        mvc.perform(patch("/api/v1/appointments/" + id + "/status").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"status\\":\\"CONFIRMED\\"}}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/appointments/" + id + "/status").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"status\\":\\"PENDING\\"}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/appointments/" + id)).andExpect(status().isForbidden());
    }}
}}
""")

# ============================ MEDICATION SERVICE (8105) ============================
SVC = "medication-service"; common_files(SVC, 8105)
P = java_pkg(SVC)
w(J(SVC, "entity/Medication.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medications")
public class Medication {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private String name;
    private String dosage;
    private String frequency;
    private String status = "ACTIVE";
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public Long getPatientId() {{ return patientId; }} public void setPatientId(Long v) {{ this.patientId = v; }}
    public String getName() {{ return name; }} public void setName(String v) {{ this.name = v; }}
    public String getDosage() {{ return dosage; }} public void setDosage(String v) {{ this.dosage = v; }}
    public String getFrequency() {{ return frequency; }} public void setFrequency(String v) {{ this.frequency = v; }}
    public String getStatus() {{ return status; }} public void setStatus(String v) {{ this.status = v; }}
}}
""")
w(J(SVC, "repo/MedicationRepository.java"), f"""package {P}.repo;

import {P}.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {{
    List<Medication> findByPatientId(Long patientId);
}}
""")
w(J(SVC, "web/MedicationController.java"), f"""package {P}.web;

import {P}.common.AuditService;
import {P}.common.EventPublisher;
import {P}.entity.Medication;
import {P}.repo.MedicationRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {{
    private static final java.util.Set<String> PRESCRIBABLE = java.util.Set.of(
            "paracetamol", "ibuprofen", "amoxicillin", "metformin", "atorvastatin",
            "aspirin", "omeprazole", "salbutamol");
    private final MedicationRepository repo;
    private final EventPublisher events;
    private final AuditService audit;
    public MedicationController(MedicationRepository repo, EventPublisher events, AuditService audit) {{
        this.repo = repo; this.events = events; this.audit = audit;
    }}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public ResponseEntity<Medication> create(@RequestBody Medication m, Authentication auth) {{
        // Safety guardrail: only formulary medications may be prescribed through the API.
        if (m.getName() == null || !PRESCRIBABLE.contains(m.getName().toLowerCase()))
            throw new IllegalArgumentException("Medication not in approved formulary - provider review required");
        if (m.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        m.setId(null); m.setStatus("ACTIVE");
        Medication saved = repo.save(m);
        audit.record(auth.getName(), "PRESCRIBE", "Medication", String.valueOf(saved.getId()), "SUCCESS");
        try {{
            events.publish(Topics.MEDICATION_CREATED, DomainEvent.of("medication.created", "Medication",
                    String.valueOf(saved.getId()), "medication-service", MDC.get("correlationId"),
                    Map.of("patientId", String.valueOf(saved.getPatientId()), "name", saved.getName())));
        }} catch (Exception ignored) {{}}
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }}

    @GetMapping("/patient/{{patientId}}")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public List<Medication> byPatient(@PathVariable Long patientId) {{
        return repo.findByPatientId(patientId);
    }}

    @PostMapping("/{{id}}/refill")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Medication refill(@PathVariable Long id, Authentication auth) {{
        Medication m = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Medication not found"));
        m.setStatus("REFILL_REQUESTED");
        Medication saved = repo.save(m);
        audit.record(auth.getName(), "REFILL", "Medication", String.valueOf(id), "SUCCESS");
        try {{
            events.publish(Topics.MEDICATION_REFILL, DomainEvent.of("medication.refill-requested", "Medication",
                    String.valueOf(id), "medication-service", MDC.get("correlationId"), Map.of()));
        }} catch (Exception ignored) {{}}
        return saved;
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic medCreated() {{ return new NewTopic("healthcare.medication.events.created", 1, (short) 1); }}
    @Bean public NewTopic medRefill() {{ return new NewTopic("healthcare.medication.events.refill-requested", 1, (short) 1); }}
}}
""")
w(T(SVC, "MedicationApiTest.java"), f"""package {P};

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MedicationApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void formularyGuardrailAndRefill() throws Exception {{
        String provider = token("dr", "PROVIDER");
        String resp = mvc.perform(post("/api/v1/medications").header("Authorization", "Bearer " + provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"patientId\\":1,\\"name\\":\\"Paracetamol\\",\\"dosage\\":\\"500mg\\",\\"frequency\\":\\"BID\\"}}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\\"id\\":")[1].split("[,}}]")[0].trim();
        mvc.perform(post("/api/v1/medications").header("Authorization", "Bearer " + provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"patientId\\":1,\\"name\\":\\"Unobtanium-X\\",\\"dosage\\":\\"1g\\"}}"))
                .andExpect(status().isBadRequest());
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/medications/" + id + "/refill").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk());
    }}
}}
""")

# ============================ BILLING SERVICE (8106) ============================
SVC = "billing-service"; common_files(SVC, 8106)
P = java_pkg(SVC)
w(J(SVC, "entity/Invoice.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "invoices")
public class Invoice {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private Double amount;
    private String status = "PENDING";
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public Long getPatientId() {{ return patientId; }} public void setPatientId(Long v) {{ this.patientId = v; }}
    public Double getAmount() {{ return amount; }} public void setAmount(Double v) {{ this.amount = v; }}
    public String getStatus() {{ return status; }} public void setStatus(String v) {{ this.status = v; }}
}}
""")
w(J(SVC, "entity/Claim.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "claims")
public class Claim {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long invoiceId;
    private String insurer;
    private String status = "SUBMITTED";
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public Long getInvoiceId() {{ return invoiceId; }} public void setInvoiceId(Long v) {{ this.invoiceId = v; }}
    public String getInsurer() {{ return insurer; }} public void setInsurer(String v) {{ this.insurer = v; }}
    public String getStatus() {{ return status; }} public void setStatus(String v) {{ this.status = v; }}
}}
""")
w(J(SVC, "repo/InvoiceRepository.java"), f"""package {P}.repo;

import {P}.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {{
}}
""")
w(J(SVC, "repo/ClaimRepository.java"), f"""package {P}.repo;

import {P}.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {{
}}
""")
w(J(SVC, "web/BillingController.java"), f"""package {P}.web;

import {P}.common.AuditService;
import {P}.common.EventPublisher;
import {P}.entity.Claim;
import {P}.entity.Invoice;
import {P}.repo.ClaimRepository;
import {P}.repo.InvoiceRepository;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {{
    private static final Set<String> PAY = Set.of("PENDING>PROCESSING", "PROCESSING>PAID", "PROCESSING>FAILED", "PENDING>CANCELLED", "FAILED>CANCELLED");
    private final InvoiceRepository invoices;
    private final ClaimRepository claims;
    private final EventPublisher events;
    private final AuditService audit;

    public BillingController(InvoiceRepository invoices, ClaimRepository claims, EventPublisher events, AuditService audit) {{
        this.invoices = invoices; this.claims = claims; this.events = events; this.audit = audit;
    }}

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Invoice> create(@RequestBody Invoice inv, Authentication auth) {{
        if (inv.getAmount() == null || inv.getAmount() <= 0) throw new IllegalArgumentException("amount must be positive");
        inv.setId(null); inv.setStatus("PENDING");
        Invoice saved = invoices.save(inv);
        audit.record(auth.getName(), "INVOICE_CREATE", "Invoice", String.valueOf(saved.getId()), "SUCCESS");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }}

    @PostMapping("/invoices/{{id}}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Invoice pay(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {{
        Invoice inv = invoices.findById(id).orElseThrow(() -> new NoSuchElementException("Invoice not found"));
        boolean ok = !"fail".equalsIgnoreCase(body.getOrDefault("outcome", "ok"));
        String step1 = next(inv.getStatus(), "PROCESSING");
        inv.setStatus(step1); invoices.save(inv);
        String terminal = ok ? "PAID" : "FAILED";
        inv.setStatus(next(inv.getStatus(), terminal));
        Invoice saved = invoices.save(inv);
        audit.record(auth.getName(), "PAY_" + terminal, "Invoice", String.valueOf(id), "SUCCESS");
        try {{
            events.publish(Topics.PAYMENT_COMPLETED, DomainEvent.of("billing.payment-" + terminal.toLowerCase(),
                    "Invoice", String.valueOf(id), "billing-service", MDC.get("correlationId"),
                    Map.of("status", terminal, "amount", String.valueOf(saved.getAmount()))));
        }} catch (Exception ignored) {{}}
        return saved;
    }}

    private String next(String from, String to) {{
        if (!PAY.contains(from + ">" + to)) throw new IllegalArgumentException("Illegal transition " + from + " -> " + to);
        return to;
    }}

    @PostMapping("/claims")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Claim> claim(@RequestBody Claim c, Authentication auth) {{
        invoices.findById(c.getInvoiceId()).orElseThrow(() -> new NoSuchElementException("Invoice not found"));
        c.setId(null); c.setStatus("SUBMITTED");
        Claim saved = claims.save(c);
        audit.record(auth.getName(), "CLAIM_SUBMIT", "Claim", String.valueOf(saved.getId()), "SUCCESS");
        try {{
            events.publish(Topics.CLAIM_SUBMITTED, DomainEvent.of("claim.submitted", "Claim",
                    String.valueOf(saved.getId()), "billing-service", MDC.get("correlationId"), Map.of()));
        }} catch (Exception ignored) {{}}
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic payCompleted() {{ return new NewTopic("healthcare.billing.events.payment-completed", 1, (short) 1); }}
    @Bean public NewTopic claimSubmitted() {{ return new NewTopic("healthcare.claim.events.submitted", 1, (short) 1); }}
}}
""")
w(T(SVC, "BillingApiTest.java"), f"""package {P};

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BillingApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void invoiceLifecycle() throws Exception {{
        String staff = token("staff", "STAFF");
        String resp = mvc.perform(post("/api/v1/billing/invoices").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"patientId\\":1,\\"amount\\":250.0}}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\\"id\\":")[1].split("[,}}]")[0].trim();
        mvc.perform(post("/api/v1/billing/invoices/" + id + "/pay").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"outcome\\":\\"ok\\"}}"))
                .andExpect(status().isOk());
        // terminal state: further pay must fail
        mvc.perform(post("/api/v1/billing/invoices/" + id + "/pay").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"outcome\\":\\"ok\\"}}"))
                .andExpect(status().isBadRequest());
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/billing/invoices").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"patientId\\":1,\\"amount\\":10.0}}"))
                .andExpect(status().isForbidden());
    }}
}}
""")

print("part4 done")
