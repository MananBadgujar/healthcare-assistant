"""Phase 13 platform scaffolder. Generates:
  platform/pom.xml (aggregator)
  platform/shared-contracts
  platform/api-gateway
  platform/<11 domain services>
  platform/docker-compose.yml, Dockerfiles, verify script, runbook
Run from repo root: python scaffold_phase13.py
"""
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "platform")
BOOT = "3.5.8"
JJWT = "0.11.5"

def w(path, content):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)

def svc_pom(artifact, desc, extra_deps=""):
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.healthcare.platform</groupId>
        <artifactId>healthcare-platform</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>{artifact}</artifactId>
    <name>{desc}</name>
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
            <artifactId>spring-boot-starter-data-jpa</artifactId>
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
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>{JJWT}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>{JJWT}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>{JJWT}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
{extra_deps}        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
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
        <finalName>{artifact}</finalName>
    </build>
</project>
"""

# ---------------------------------------------------------------- parent pom
w("pom.xml", f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.healthcare.platform</groupId>
    <artifactId>healthcare-platform</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>Healthcare Platform (Phase 13 Microservices)</name>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>{BOOT}</version>
        <relativePath/>
    </parent>
    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <modules>
        <module>shared-contracts</module>
        <module>api-gateway</module>
        <module>auth-service</module>
        <module>patient-service</module>
        <module>provider-service</module>
        <module>appointment-service</module>
        <module>medication-service</module>
        <module>billing-service</module>
        <module>ai-service</module>
        <module>rag-service</module>
        <module>cds-service</module>
        <module>notification-service</module>
        <module>inventory-service</module>
    </modules>
</project>
""")

# ------------------------------------------------------- shared contracts
w("shared-contracts/pom.xml", f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.healthcare.platform</groupId>
        <artifactId>healthcare-platform</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>shared-contracts</artifactId>
    <name>Shared Contracts (events, errors, correlation)</name>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
""")
w("shared-contracts/src/main/java/com/healthcare/contracts/DomainEvent.java",
"""package com.healthcare.contracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Versioned Kafka domain-event envelope shared by all services. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainEvent {
    private String eventId;
    private String eventName;
    private String eventVersion = "v1";
    private Instant occurredAt;
    private String correlationId;
    private String entityType;
    private String entityId;
    private String producer;
    private Map<String, Object> payload;

    public DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    public static DomainEvent of(String eventName, String entityType, String entityId,
                                 String producer, String correlationId, Map<String, Object> payload) {
        DomainEvent e = new DomainEvent();
        e.eventName = eventName;
        e.entityType = entityType;
        e.entityId = entityId;
        e.producer = producer;
        e.correlationId = correlationId;
        e.payload = payload;
        return e;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventVersion() { return eventVersion; }
    public void setEventVersion(String eventVersion) { this.eventVersion = eventVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
""")
w("shared-contracts/src/main/java/com/healthcare/contracts/ApiError.java",
"""package com.healthcare.contracts;

import java.time.Instant;

/** Centralized API error model used by gateway and every service. */
public class ApiError {
    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String correlationId;
    private String service;

    public ApiError() {}

    public ApiError(int status, String code, String message, String correlationId, String service) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.code = code;
        this.message = message;
        this.correlationId = correlationId;
        this.service = service;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}
""")
w("shared-contracts/src/main/java/com/healthcare/contracts/Correlation.java",
"""package com.healthcare.contracts;

import java.util.UUID;

/** Correlation-id helpers. Header name is shared by gateway and all services. */
public final class Correlation {
    public static final String HEADER = "X-Correlation-Id";
    private Correlation() {}

    public static String ensure(String incoming) {
        return (incoming == null || incoming.isBlank()) ? UUID.randomUUID().toString() : incoming;
    }
}
""")
w("shared-contracts/src/main/java/com/healthcare/contracts/Topics.java",
"""package com.healthcare.contracts;

/** Canonical Kafka topic names for the platform. */
public final class Topics {
    private Topics() {}
    public static final String PATIENT_CREATED = "healthcare.patient.events.created";
    public static final String PATIENT_UPDATED = "healthcare.patient.events.updated";
    public static final String APPOINTMENT_CREATED = "healthcare.appointment.events.created";
    public static final String APPOINTMENT_CANCELLED = "healthcare.appointment.events.cancelled";
    public static final String APPOINTMENT_STATUS = "healthcare.appointment.events.status-changed";
    public static final String MEDICATION_CREATED = "healthcare.medication.events.created";
    public static final String MEDICATION_REFILL = "healthcare.medication.events.refill-requested";
    public static final String PAYMENT_COMPLETED = "healthcare.billing.events.payment-completed";
    public static final String CLAIM_SUBMITTED = "healthcare.claim.events.submitted";
    public static final String AI_TRIAGE_COMPLETED = "healthcare.ai.events.triage-completed";
    public static final String CLINICAL_ALERT = "healthcare.encounter.events.clinical-alert";
    public static final String LOW_STOCK = "healthcare.inventory.events.low-stock";
    public static final String NEAR_EXPIRY = "healthcare.inventory.events.near-expiry";
    public static final String AUDIT_EVENTS = "healthcare.audit.events";
}
""")
w("shared-contracts/src/test/java/com/healthcare/contracts/DomainEventTest.java",
"""package com.healthcare.contracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class DomainEventTest {
    @Test
    public void envelopeSerializesWithAllRequiredFields() throws Exception {
        DomainEvent e = DomainEvent.of("appointment.created", "Appointment", "42",
                "appointment-service", "corr-1", Map.of("status", "PENDING"));
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = om.writeValueAsString(e);
        DomainEvent back = om.readValue(json, DomainEvent.class);
        assertNotNull(back.getEventId());
        assertEquals("v1", back.getEventVersion());
        assertEquals("appointment.created", back.getEventName());
        assertEquals("corr-1", back.getCorrelationId());
        assertEquals("42", back.getEntityId());
        assertNotNull(back.getOccurredAt());
    }
}
""")

print("part1 done")
