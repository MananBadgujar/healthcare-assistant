"""Phase 13 scaffolder part 5: ai, rag, cds, notification, inventory."""
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
TEST_IMPORTS = """import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import java.util.Date;
import java.util.List;
"""

# ============================ AI SERVICE (8107) ============================
SVC = "ai-service"
common_files(SVC, 8107, "rag.service.base-url=${RAG_URL:http://localhost:8108}\ncds.service.base-url=${CDS_URL:http://localhost:8109}\n")
P = java_pkg(SVC)
w(J(SVC, "service/AiService.java"), f"""package {P}.service;

import {P}.common.ServiceClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {{
    private static final List<String> RED_FLAGS = List.of(
            "chest pain", "difficulty breathing", "shortness of breath", "stroke",
            "unconscious", "severe bleeding", "suicidal", "heart attack");
    private static final List<String> NON_HEALTH = List.of(
            "stock price", "football", "election", "crypto", "movie", "lottery");
    private final ServiceClients clients;
    private final String ragBase;
    private final String cdsBase;

    public AiService(ServiceClients clients,
                     @Value("${{rag.service.base-url}}") String ragBase,
                     @Value("${{cds.service.base-url}}") String cdsBase) {{
        this.clients = clients; this.ragBase = ragBase; this.cdsBase = cdsBase;
    }}

    public Map<String, Object> triage(String symptoms, String authHeader) {{
        String lower = (symptoms == null ? "" : symptoms.toLowerCase());
        // Guardrail 1: healthcare-topic restriction
        for (String n : NON_HEALTH) {{
            if (lower.contains(n))
                return Map.of("refused", true,
                        "message", "This assistant only answers healthcare-related questions.");
        }}
        if (lower.isBlank())
            throw new IllegalArgumentException("symptoms text required");
        // Guardrail 2: red-flag / emergency escalation
        List<String> flags = new ArrayList<>();
        for (String r : RED_FLAGS) if (lower.contains(r)) flags.add(r);
        String urgency = flags.isEmpty() ? (lower.contains("fever") || lower.contains("pain") ? "MODERATE" : "LOW") : "EMERGENCY";
        // RAG boundary (resilient: fallback when unavailable)
        List<String> sources = new ArrayList<>();
        try {{
            var resp = clients.call(ragBase, "/api/v1/rag/search?q=" + java.net.URLEncoder.encode(lower, "UTF-8"),
                    org.springframework.http.HttpMethod.GET, null, authHeader);
            sources.add("rag-service:consulted");
        }} catch (Exception e) {{
            sources.add("rag-service:unavailable-fallback");
        }}
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("refused", false);
        out.put("urgency", urgency);
        out.put("redFlags", flags);
        out.put("escalateToEmergency", !flags.isEmpty());
        out.put("sources", sources);
        out.put("disclaimer", "AI triage support only - provider review required.");
        return out;
    }}

    public Map<String, Object> labInterpret(String text) {{
        if (text == null || text.isBlank()) throw new IllegalArgumentException("lab text required");
        String lower = text.toLowerCase();
        List<String> alerts = new ArrayList<>();
        if (lower.contains("critical") || lower.contains("panic value")) alerts.add("CRITICAL_VALUE");
        if (lower.contains("high") && lower.contains("glucose")) alerts.add("HYPERGLYCEMIA_FLAG");
        return Map.of("alerts", alerts, "requiresProviderReview", true,
                "summary", alerts.isEmpty() ? "No critical flags detected." : "Flags detected: " + alerts);
    }}
}}
""")
w(J(SVC, "web/AiController.java"), f"""package {P}.web;

import {P}.common.AuditService;
import {P}.common.EventPublisher;
import {P}.service.AiService;
import com.healthcare.contracts.DomainEvent;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {{
    private final AiService ai;
    private final EventPublisher events;
    private final AuditService audit;
    public AiController(AiService ai, EventPublisher events, AuditService audit) {{
        this.ai = ai; this.events = events; this.audit = audit;
    }}

    @PostMapping("/triage")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Map<String, Object>> triage(@RequestBody Map<String, String> body,
                                                      Authentication auth,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {{
        Map<String, Object> out = ai.triage(body.get("symptoms"), authHeader);
        audit.record(auth.getName(), "TRIAGE", "Triage", "-", "SUCCESS");
        try {{
            events.publish("healthcare.ai.events.triage-completed",
                    DomainEvent.of("ai.triage-completed", "Triage", auth.getName(), "ai-service",
                            MDC.get("correlationId"), Map.of("urgency", String.valueOf(out.get("urgency")))));
        }} catch (Exception ignored) {{}}
        return ResponseEntity.ok(out);
    }}

    @PostMapping("/lab-interpret")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Map<String, Object> lab(@RequestBody Map<String, String> body) {{
        return ai.labInterpret(body.get("text"));
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic triageDone() {{ return new NewTopic("healthcare.ai.events.triage-completed", 1, (short) 1); }}
}}
""")
w(T(SVC, "AiApiTest.java"), f"""package {P};

{TEST_IMPORTS}import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AiApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void guardrailsHold() throws Exception {{
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/ai/triage").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"symptoms\\":\\"I have chest pain and difficulty breathing\\"}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.urgency").value("EMERGENCY"));
        mvc.perform(post("/api/v1/ai/triage").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"symptoms\\":\\"who will win the football match\\"}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refused").value(true));
        mvc.perform(post("/api/v1/ai/triage")).andExpect(status().isForbidden());
    }}
}}
""")

# ============================ RAG SERVICE (8108) ============================
SVC = "rag-service"; common_files(SVC, 8108)
P = java_pkg(SVC)
w(J(SVC, "entity/KbDocument.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kb_documents")
public class KbDocument {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(length = 20000)
    private String content;
    private int version = 1;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public String getTitle() {{ return title; }} public void setTitle(String v) {{ this.title = v; }}
    public String getContent() {{ return content; }} public void setContent(String v) {{ this.content = v; }}
    public int getVersion() {{ return version; }} public void setVersion(int v) {{ this.version = v; }}
}}
""")
w(J(SVC, "entity/KbChunk.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kb_chunks")
public class KbChunk {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long documentId;
    @Column(length = 5000)
    private String text;
    @Column(length = 20000)
    private String embedding;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public Long getDocumentId() {{ return documentId; }} public void setDocumentId(Long v) {{ this.documentId = v; }}
    public String getText() {{ return text; }} public void setText(String v) {{ this.text = v; }}
    public String getEmbedding() {{ return embedding; }} public void setEmbedding(String v) {{ this.embedding = v; }}
}}
""")
w(J(SVC, "repo/KbDocumentRepository.java"), f"""package {P}.repo;

import {P}.entity.KbDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {{
}}
""")
w(J(SVC, "repo/KbChunkRepository.java"), f"""package {P}.repo;

import {P}.entity.KbChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KbChunkRepository extends JpaRepository<KbChunk, Long> {{
    List<KbChunk> findByDocumentId(Long documentId);
}}
""")
w(J(SVC, "service/RagService.java"), f"""package {P}.service;

import {P}.entity.KbChunk;
import {P}.entity.KbDocument;
import {P}.repo.KbChunkRepository;
import {P}.repo.KbDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RagService {{
    static final int DIM = 64;
    private final KbDocumentRepository docs;
    private final KbChunkRepository chunks;

    public RagService(KbDocumentRepository docs, KbChunkRepository chunks) {{
        this.docs = docs; this.chunks = chunks;
    }}

    public static double[] embed(String text) {{
        double[] v = new double[DIM];
        String[] toks = text.toLowerCase().split("[^a-z0-9]+");
        for (String t : toks) {{
            if (t.isBlank()) continue;
            int h = Math.abs(t.hashCode());
            v[h % DIM] += 1.0;
        }}
        double n = 0; for (double d : v) n += d * d;
        n = Math.sqrt(n);
        if (n > 0) for (int i = 0; i < DIM; i++) v[i] /= n;
        return v;
    }}

    static String pack(double[] v) {{
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++) {{ if (i > 0) sb.append(','); sb.append(v[i]); }}
        return sb.toString();
    }}

    static double[] unpack(String s) {{
        String[] p = s.split(",");
        double[] v = new double[p.length];
        for (int i = 0; i < p.length; i++) v[i] = Double.parseDouble(p[i]);
        return v;
    }}

    @Transactional
    public KbDocument ingest(String title, String content) {{
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content required");
        KbDocument d = new KbDocument();
        d.setTitle(title); d.setContent(content);
        KbDocument saved = docs.save(d);
        for (String para : content.split("\\n\\n")) {{
            if (para.isBlank()) continue;
            KbChunk c = new KbChunk();
            c.setDocumentId(saved.getId());
            c.setText(para.length() > 4000 ? para.substring(0, 4000) : para);
            c.setEmbedding(pack(embed(para)));
            chunks.save(c);
        }}
        return saved;
    }}

    public List<Map<String, Object>> search(String q, int topK) {{
        double[] qv = embed(q == null ? "" : q);
        List<Map<String, Object>> scored = new ArrayList<>();
        for (KbChunk c : chunks.findAll()) {{
            double[] v = unpack(c.getEmbedding());
            double dot = 0; for (int i = 0; i < Math.min(qv.length, v.length); i++) dot += qv[i] * v[i];
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("documentId", c.getDocumentId());
            m.put("text", c.getText());
            m.put("score", dot);
            scored.add(m);
        }}
        scored.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return scored.subList(0, Math.min(topK, scored.size()));
    }}
}}
""")
w(J(SVC, "web/RagController.java"), f"""package {P}.web;

import {P}.entity.KbDocument;
import {P}.service.RagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {{
    private final RagService rag;
    public RagController(RagService rag) {{ this.rag = rag; }}

    @PostMapping("/ingest")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody Map<String, String> body) {{
        KbDocument d = rag.ingest(body.get("title"), body.get("content"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", d.getId(), "title", d.getTitle()));
    }}

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public List<Map<String, Object>> search(@RequestParam("q") String q,
                                            @RequestParam(defaultValue = "3") int topK) {{
        return rag.search(q, Math.min(topK, 10));
    }}

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Map<String, Object> ask(@RequestBody Map<String, String> body) {{
        List<Map<String, Object>> hits = rag.search(body.getOrDefault("q", ""), 3);
        return Map.of("answer", hits.isEmpty() ? "No relevant knowledge found." : "See cited sources.",
                "citations", hits);
    }}
}}
""")
w(T(SVC, "RagApiTest.java"), f"""package {P};

{TEST_IMPORTS}import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RagApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void ingestSearchCite() throws Exception {{
        String staff = token("s", "STAFF");
        mvc.perform(post("/api/v1/rag/ingest").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"title\\":\\"Hypertension Guide\\",\\"content\\":\\"Hypertension management includes low sodium diet.\\n\\nBeta blockers reduce blood pressure.\\"}}"))
                .andExpect(status().isCreated());
        String patient = token("p", "PATIENT");
        mvc.perform(get("/api/v1/rag/search?q=blood+pressure").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].documentId").exists());
        mvc.perform(post("/api/v1/rag/ask").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"q\\":\\"blood pressure\\"}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.citations").exists());
    }}
}}
""")

# ============================ CDS SERVICE (8109) ============================
SVC = "cds-service"; common_files(SVC, 8109)
P = java_pkg(SVC)
w(J(SVC, "service/CdsService.java"), f"""package {P}.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CdsService {{
    private static final Map<String, String> PAIRS = Map.of(
            "warfarin+aspirin", "MAJOR",
            "warfarin+ibuprofen", "MAJOR",
            "simvastatin+clarithromycin", "MAJOR",
            "metformin+contrast", "MODERATE",
            "lisinopril+potassium", "MODERATE");

    public Map<String, Object> check(List<String> drugs) {{
        if (drugs == null || drugs.size() < 2) throw new IllegalArgumentException("At least two drugs required");
        List<String> norm = drugs.stream().map(d -> d.toLowerCase().trim()).sorted().toList();
        List<Map<String, String>> alerts = new ArrayList<>();
        for (int i = 0; i < norm.size(); i++) for (int j = i + 1; j < norm.size(); j++) {{
            String key = norm.get(i) + "+" + norm.get(j);
            String sev = PAIRS.get(key);
            if (sev != null) alerts.add(Map.of("pair", key, "severity", sev,
                    "evidence", "CDS knowledge base v1"));
        }}
        String risk = alerts.stream().anyMatch(a -> a.get("severity").equals("MAJOR")) ? "HIGH"
                : alerts.isEmpty() ? "LOW" : "MODERATE";
        return Map.of("alerts", alerts, "risk", risk,
                "recommendation", alerts.isEmpty() ? "No known interactions." : "Provider review required before ordering.",
                "requiresProviderReview", true);
    }}
}}
""")
w(J(SVC, "web/CdsController.java"), f"""package {P}.web;

import {P}.service.CdsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cds")
public class CdsController {{
    private final CdsService cds;
    public CdsController(CdsService cds) {{ this.cds = cds; }}

    @PostMapping("/check-interactions")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Map<String, Object> check(@RequestBody Map<String, List<String>> body) {{
        return cds.check(body.get("drugs"));
    }}

    @PostMapping("/contraindications/check")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Map<String, Object> contra(@RequestBody Map<String, Object> body) {{
        return Map.of("alerts", List.of(), "requiresProviderReview", true);
    }}
}}
""")
w(T(SVC, "CdsApiTest.java"), f"""package {P};

{TEST_IMPORTS}import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CdsApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void detectsMajorInteraction() throws Exception {{
        String provider = token("dr", "PROVIDER");
        mvc.perform(post("/api/v1/cds/check-interactions").header("Authorization", "Bearer " + provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"drugs\\":[\\"warfarin\\",\\"aspirin\\"]}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risk").value("HIGH"))
                .andExpect(jsonPath("$.requiresProviderReview").value(true));
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/cds/check-interactions").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"drugs\\":[\\"warfarin\\",\\"aspirin\\"]}}"))
                .andExpect(status().isForbidden());
    }}
}}
""")

# ============================ NOTIFICATION SERVICE (8110) ============================
SVC = "notification-service"; common_files(SVC, 8110)
P = java_pkg(SVC)
w(J(SVC, "entity/Notification.java"), f"""package {P}.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String eventId;
    private String type;
    @Column(length = 4000)
    private String message;
    private String recipient;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public String getEventId() {{ return eventId; }} public void setEventId(String v) {{ this.eventId = v; }}
    public String getType() {{ return type; }} public void setType(String v) {{ this.type = v; }}
    public String getMessage() {{ return message; }} public void setMessage(String v) {{ this.message = v; }}
    public String getRecipient() {{ return recipient; }} public void setRecipient(String v) {{ this.recipient = v; }}
}}
""")
w(J(SVC, "repo/NotificationRepository.java"), f"""package {P}.repo;

import {P}.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {{
    boolean existsByEventId(String eventId);
}}
""")
w(J(SVC, "service/NotificationConsumer.java"), f"""package {P}.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import {P}.entity.Notification;
import {P}.repo.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationConsumer {{
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationRepository repo;
    private final ObjectMapper om;

    public NotificationConsumer(NotificationRepository repo, ObjectMapper om) {{
        this.repo = repo; this.om = om;
    }}

    @KafkaListener(topics = {{
            "healthcare.appointment.events.created",
            "healthcare.appointment.events.cancelled",
            "healthcare.medication.events.refill-requested",
            "healthcare.billing.events.payment-completed",
            "healthcare.encounter.events.clinical-alert",
            "healthcare.ai.events.triage-completed"
    }}, groupId = "notification-service-group")
    @Transactional
    public void onEvent(String raw) {{
        try {{
            DomainEvent e = om.readValue(raw, DomainEvent.class);
            if (e.getEventId() == null) {{ log.warn("notification: dropping event without id"); return; }}
            // Idempotent consumption: duplicates are acknowledged without side effects.
            if (repo.existsByEventId(e.getEventId())) {{
                log.info("notification: duplicate {{}} ignored", e.getEventId());
                return;
            }}
            Notification n = new Notification();
            n.setEventId(e.getEventId());
            n.setType(e.getEventName());
            n.setRecipient(String.valueOf(e.getPayload() == null ? "" : e.getPayload().getOrDefault("recipient", "")));
            n.setMessage("[" + e.getEventName() + "] entity " + e.getEntityType() + ":" + e.getEntityId());
            repo.save(n);
            log.info("notification: stored {{}} corr={{}}", e.getEventName(), e.getCorrelationId());
        }} catch (Exception ex) {{
            // Invalid events are logged and skipped so they never break the domain transaction.
            log.warn("notification: invalid event skipped: {{}}", ex.toString());
        }}
    }}
}}
""")
w(J(SVC, "web/NotificationController.java"), f"""package {P}.web;

import {P}.entity.Notification;
import {P}.repo.NotificationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {{
    private final NotificationRepository repo;
    public NotificationController(NotificationRepository repo) {{ this.repo = repo; }}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER','PATIENT')")
    public List<Notification> all() {{
        return repo.findAll();
    }}
}}
""")
w(T(SVC, "NotificationKafkaTest.java"), f"""package {P};

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import {P}.repo.NotificationRepository;
import {P}.service.NotificationConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {{"healthcare.appointment.events.created"}})
public class NotificationKafkaTest {{
    static final BlockingQueue<String> SEEN = new ArrayBlockingQueue<>(10);

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ObjectMapper om;
    @Autowired NotificationConsumer consumer;
    @Autowired NotificationRepository repo;

    @KafkaListener(topics = "healthcare.appointment.events.created", groupId = "phase13-notif-test")
    public void probe(ConsumerRecord<String, String> rec) {{ SEEN.offer(rec.value()); }}

    @Test
    public void consumerIsIdempotent() throws Exception {{
        DomainEvent e = DomainEvent.of("appointment.created", "Appointment", "9",
                "appointment-service", "corr-n", Map.of());
        String raw = om.writeValueAsString(e);
        kafka.send("healthcare.appointment.events.created", "9", raw);
        assertNotNull(SEEN.poll(15, TimeUnit.SECONDS), "event must reach kafka");
        consumer.onEvent(raw);
        consumer.onEvent(raw); // duplicate
        assertEquals(1, repo.findAll().size(), "duplicate event must not create a second notification");
    }}
}}
""")

# ============================ INVENTORY SERVICE (8111) ============================
SVC = "inventory-service"; common_files(SVC, 8111)
P = java_pkg(SVC)
w(J(SVC, "entity/StockItem.java"), f"""package {P}.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_items")
public class StockItem {{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String sku;
    private String name;
    private int quantity;
    private int reorderThreshold = 10;
    private LocalDate expiryDate;
    public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }}
    public String getSku() {{ return sku; }} public void setSku(String v) {{ this.sku = v; }}
    public String getName() {{ return name; }} public void setName(String v) {{ this.name = v; }}
    public int getQuantity() {{ return quantity; }} public void setQuantity(int v) {{ this.quantity = v; }}
    public int getReorderThreshold() {{ return reorderThreshold; }} public void setReorderThreshold(int v) {{ this.reorderThreshold = v; }}
    public LocalDate getExpiryDate() {{ return expiryDate; }} public void setExpiryDate(LocalDate v) {{ this.expiryDate = v; }}
}}
""")
w(J(SVC, "repo/StockRepository.java"), f"""package {P}.repo;

import {P}.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockItem, Long> {{
}}
""")
w(J(SVC, "web/InventoryController.java"), f"""package {P}.web;

import {P}.common.AuditService;
import {P}.common.EventPublisher;
import {P}.entity.StockItem;
import {P}.repo.StockRepository;
import com.healthcare.contracts.DomainEvent;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {{
    private final StockRepository repo;
    private final EventPublisher events;
    private final AuditService audit;
    public InventoryController(StockRepository repo, EventPublisher events, AuditService audit) {{
        this.repo = repo; this.events = events; this.audit = audit;
    }}

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<StockItem> create(@RequestBody StockItem item, Authentication auth) {{
        if (item.getSku() == null || item.getSku().isBlank()) throw new IllegalArgumentException("sku required");
        item.setId(null);
        StockItem saved = repo.save(item);
        audit.record(auth.getName(), "STOCK_CREATE", "StockItem", String.valueOf(saved.getId()), "SUCCESS");
        maybeEmit(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }}

    @PostMapping("/items/{{id}}/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public StockItem stockOut(@PathVariable Long id, @RequestBody Map<String, Integer> body, Authentication auth) {{
        StockItem item = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Item not found"));
        int qty = body.getOrDefault("quantity", 1);
        if (qty <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (item.getQuantity() < qty) throw new IllegalArgumentException("Insufficient stock");
        item.setQuantity(item.getQuantity() - qty);
        StockItem saved = repo.save(item);
        audit.record(auth.getName(), "STOCK_OUT", "StockItem", String.valueOf(id), "SUCCESS");
        maybeEmit(saved);
        return saved;
    }}

    @GetMapping("/reorder-analysis")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public List<Map<String, Object>> reorder() {{
        List<Map<String, Object>> out = new ArrayList<>();
        for (StockItem i : repo.findAll()) {{
            if (i.getQuantity() <= i.getReorderThreshold())
                out.add(Map.of("sku", i.getSku(), "quantity", i.getQuantity(),
                        "recommendation", "REORDER", "forecast7d", Math.max(0, i.getReorderThreshold() * 2 - i.getQuantity())));
        }}
        return out;
    }}

    @GetMapping("/expiry-alerts")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public List<Map<String, Object>> expiry() {{
        LocalDate soon = LocalDate.now().plusDays(30);
        List<Map<String, Object>> out = new ArrayList<>();
        for (StockItem i : repo.findAll()) {{
            if (i.getExpiryDate() != null && !i.getExpiryDate().isAfter(soon))
                out.add(Map.of("sku", i.getSku(), "expiryDate", String.valueOf(i.getExpiryDate()), "alert", "NEAR_EXPIRY"));
        }}
        return out;
    }}

    private void maybeEmit(StockItem item) {{
        try {{
            if (item.getQuantity() <= item.getReorderThreshold())
                events.publish("healthcare.inventory.events.low-stock",
                        DomainEvent.of("inventory.low-stock", "StockItem", String.valueOf(item.getId()),
                                "inventory-service", MDC.get("correlationId"),
                                Map.of("sku", item.getSku() == null ? "" : item.getSku())));
        }} catch (Exception ignored) {{}}
    }}
}}
""")
w(J(SVC, "config/KafkaTopics.java"), f"""package {P}.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {{
    @Bean public NewTopic lowStock() {{ return new NewTopic("healthcare.inventory.events.low-stock", 1, (short) 1); }}
    @Bean public NewTopic nearExpiry() {{ return new NewTopic("healthcare.inventory.events.near-expiry", 1, (short) 1); }}
}}
""")
w(T(SVC, "InventoryApiTest.java"), f"""package {P};

{TEST_IMPORTS}import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryApiTest {{
    @Autowired MockMvc mvc;
{TOKEN_HELPER}
    @Test
    public void stockAndAlerts() throws Exception {{
        String staff = token("s", "STAFF");
        String resp = mvc.perform(post("/api/v1/inventory/items").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{{\\"sku\\":\\"AMOX-500\\",\\"name\\":\\"Amoxicillin\\",\\"quantity\\":5,\\"reorderThreshold\\":10}}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\\"id\\":")[1].split("[,}}]")[0].trim();
        mvc.perform(get("/api/v1/inventory/reorder-analysis").header("Authorization", "Bearer " + staff))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].recommendation").value("REORDER"));
        mvc.perform(post("/api/v1/inventory/items/" + id + "/stock-out").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{{\\"quantity\\":99}}"))
                .andExpect(status().isBadRequest());
    }}
}}
""")

print("part5 done")
