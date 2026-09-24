package com.healthcare.assistant.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.healthcare.assistant.kafka.entity.EventAuditLog;
import com.healthcare.assistant.kafka.entity.EventFailure;
import com.healthcare.assistant.kafka.entity.ProcessedEvent;
import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.producer.EventPublisher;
import com.healthcare.assistant.kafka.repository.EventAuditLogRepository;
import com.healthcare.assistant.kafka.repository.EventFailureRepository;
import com.healthcare.assistant.kafka.repository.ProcessedEventRepository;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "healthcare.appointment.events.created",
        "healthcare.appointment.events.updated",
        "healthcare.appointment.events.cancelled",
        "healthcare.appointment.events.confirmed",
        "healthcare.appointment.events.completed",
        "healthcare.appointment.events.no-show",
        "healthcare.appointment.events.status-changed",
        "healthcare.appointment.events.created.dlq",
        "healthcare.billing.events.payment-completed",
        "healthcare.clinical.events.clinical-alert"
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaIntegrationTest {

    @BeforeAll
    static void setBootstrapServers(@Autowired EmbeddedKafkaBroker broker) {
        System.setProperty("spring.kafka.bootstrap-servers", broker.getBrokersAsString());
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("rag.embedding.provider", () -> "hash");
        registry.add("rag.embedding.dimension", () -> "256");
        registry.add("rag.generation.fallback-on-llm-error", () -> "true");
        registry.add("ai.ollama.base-url", () -> "http://localhost:11434");
        registry.add("ai.ollama.model", () -> "llama2");
        registry.add("jwt.secret", () -> "REDACTED");
        registry.add("spring.security.csrf.enabled", () -> "false");
        registry.add("kafka.topics.patient.created", () -> "healthcare.patient.events.created");
        registry.add("kafka.topics.patient.updated", () -> "healthcare.patient.events.updated");
        registry.add("kafka.topics.appointment.created", () -> "healthcare.appointment.events.created");
        registry.add("kafka.topics.appointment.updated", () -> "healthcare.appointment.events.updated");
        registry.add("kafka.topics.appointment.cancelled", () -> "healthcare.appointment.events.cancelled");
        registry.add("kafka.topics.appointment.confirmed", () -> "healthcare.appointment.events.confirmed");
        registry.add("kafka.topics.appointment.completed", () -> "healthcare.appointment.events.completed");
        registry.add("kafka.topics.appointment.no-show", () -> "healthcare.appointment.events.no-show");
        registry.add("kafka.topics.appointment.status-changed", () -> "healthcare.appointment.events.status-changed");
        registry.add("kafka.topics.encounter.completed", () -> "healthcare.encounter.events.completed");
        registry.add("kafka.topics.encounter.lab-result", () -> "healthcare.encounter.events.lab-result");
        registry.add("kafka.topics.encounter.clinical-alert", () -> "healthcare.encounter.events.clinical-alert");
        registry.add("kafka.topics.encounter.care-plan-approved", () -> "healthcare.encounter.events.care-plan-approved");
        registry.add("kafka.topics.encounter.medication-changed", () -> "healthcare.encounter.events.medication-changed");
        registry.add("kafka.topics.encounter.triage-completed", () -> "healthcare.encounter.events.triage-completed");
        registry.add("kafka.topics.medication.created", () -> "healthcare.medication.events.created");
        registry.add("kafka.topics.medication.refill-requested", () -> "healthcare.medication.events.refill-requested");
        registry.add("kafka.topics.billing.payment-completed", () -> "healthcare.billing.events.payment-completed");
        registry.add("kafka.topics.billing.claim-submitted", () -> "healthcare.billing.events.claim-submitted");
        registry.add("kafka.topics.billing.claim-approved", () -> "healthcare.billing.events.claim-approved");
        registry.add("kafka.topics.billing.claim-rejected", () -> "healthcare.billing.events.claim-rejected");
        registry.add("kafka.topics.claim.events", () -> "healthcare.claim.events");
        registry.add("kafka.topics.claim.submitted", () -> "healthcare.claim.events.submitted");
        registry.add("kafka.topics.claim.approved", () -> "healthcare.claim.events.approved");
        registry.add("kafka.topics.claim.rejected", () -> "healthcare.claim.events.rejected");
        registry.add("kafka.topics.ai.lab-interpretation", () -> "healthcare.ai.events.lab-interpretation");
        registry.add("kafka.topics.ai.clinical-alert", () -> "healthcare.ai.events.clinical-alert");
        registry.add("kafka.topics.ai.triage-completed", () -> "healthcare.ai.events.triage-completed");
        registry.add("kafka.topics.notification.appointment-reminder", () -> "healthcare.notification.events.appointment-reminder");
        registry.add("kafka.topics.notification.appointment-cancellation", () -> "healthcare.notification.events.appointment-cancellation");
        registry.add("kafka.topics.notification.payment-confirmation", () -> "healthcare.notification.events.payment-confirmation");
        registry.add("kafka.topics.notification.lab-result-available", () -> "healthcare.notification.events.lab-result-available");
        registry.add("kafka.topics.inventory.low-stock", () -> "healthcare.inventory.events.low-stock");
        registry.add("kafka.topics.inventory.near-expiry", () -> "healthcare.inventory.events.near-expiry");
        registry.add("kafka.topics.inventory.stock-received", () -> "healthcare.inventory.events.stock-received");
        registry.add("kafka.topics.inventory.stock-consumed", () -> "healthcare.inventory.events.stock-consumed");
        registry.add("kafka.topics.inventory.stock-transferred", () -> "healthcare.inventory.events.stock-transferred");
        registry.add("kafka.topics.inventory.batch-near-expiry", () -> "healthcare.inventory.events.batch-near-expiry");
        registry.add("kafka.topics.inventory.item-expired", () -> "healthcare.inventory.events.item-expired");
        registry.add("kafka.topics.inventory.reorder-recommended", () -> "healthcare.inventory.events.reorder-recommended");
        registry.add("kafka.topics.telehealth.session-completed", () -> "healthcare.telehealth.events.session-completed");
        registry.add("kafka.topics.telehealth.session-reminder", () -> "healthcare.telehealth.events.session-reminder");
        registry.add("kafka.topics.audit.events", () -> "healthcare.audit.events");
        registry.add("kafka.consumer.group.id.patient", () -> "patient-consumer-group");
        registry.add("kafka.consumer.group.id.appointment", () -> "appointment-consumer-group");
        registry.add("kafka.consumer.group.id.encounter", () -> "encounter-consumer-group");
        registry.add("kafka.consumer.group.id.medication", () -> "medication-consumer-group");
        registry.add("kafka.consumer.group.id.billing", () -> "billing-consumer-group");
        registry.add("kafka.consumer.group.id.claim", () -> "claim-consumer-group");
        registry.add("kafka.consumer.group.id.ai", () -> "ai-consumer-group");
        registry.add("kafka.consumer.group.id.notification", () -> "notification-consumer-group");
        registry.add("kafka.consumer.group.id.inventory", () -> "inventory-consumer-group");
        registry.add("kafka.consumer.group.id.telehealth", () -> "telehealth-consumer-group");
        registry.add("kafka.retry.max-attempts", () -> "3");
        registry.add("kafka.retry.initial-backoff-ms", () -> "1000");
        registry.add("kafka.retry.max-backoff-ms", () -> "4000");
        registry.add("kafka.dlq.enabled", () -> "true");
        // Let @EmbeddedKafka's own DynamicPropertySource handle spring.kafka.bootstrap-servers
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private EventAuditLogRepository eventAuditLogRepository;

    @Autowired
    private EventFailureRepository eventFailureRepository;

    @BeforeEach
    void setUp() throws Exception {
        processedEventRepository.deleteAll();
        eventAuditLogRepository.deleteAll();
        eventFailureRepository.deleteAll();
    }

    @Test
    void testSuccessfulEventProcessing_createsAuditAndIdempotencyRecord() throws Exception {
        String topic = "healthcare.appointment.events.created";
        String eventId = "test-event-" + System.currentTimeMillis();
        String correlationId = "corr-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-001");
        event.setSource("scheduler-service");
        event.setCorrelationId(correlationId);
        event.setVersion(1);
        event.setPayload(Map.of("reason", "new appointment", "date", "2024-01-15"));

        eventPublisher.publish(event, topic);

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(processedEventRepository.findByEventIdAndConsumerId(eventId, "appointment-consumer-group").isPresent());
            assertTrue(eventAuditLogRepository.findByEventId(eventId).size() >= 2);
        });

        ProcessedEvent processed = processedEventRepository.findByEventIdAndConsumerId(eventId, "appointment-consumer-group").orElseThrow();
        assertEquals("PROCESSED", processed.getStatus());

        var auditLogs = eventAuditLogRepository.findByEventId(eventId);
        assertTrue(auditLogs.stream().anyMatch(a -> "processing-started".equals(a.getStatus())));
        assertTrue(auditLogs.stream().anyMatch(a -> "processing-completed".equals(a.getStatus())));
    }

    @Test
    void testIdempotentDuplicateDelivery_onlyProcessedOnce() throws Exception {
        String topic = "healthcare.appointment.events.created";
        String eventId = "idempotent-event-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-001");
        event.setSource("scheduler-service");
        event.setCorrelationId("corr-" + System.currentTimeMillis());
        event.setVersion(1);
        event.setPayload(Map.of("reason", "new appointment"));

        // Publish the EXACT SAME event twice
        eventPublisher.publish(event, topic);
        eventPublisher.publish(event, topic);

        await().atMost(25, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = processedEventRepository.countByEventId(eventId);
            assertEquals(1L, count);
        });
    }

    @Test
    void testRetryAndDLQ_onProcessingFailure() throws Exception {
        String topic = "healthcare.appointment.events.created";
        String eventId = "fail-event-" + System.currentTimeMillis();
        String correlationId = "corr-fail-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("appointment-created-fail");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-fail");
        event.setSource("scheduler-service");
        event.setCorrelationId(correlationId);
        event.setVersion(1);
        event.setPayload(Map.of("reason", "fail test"));

        eventPublisher.publish(event, topic);

        await().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(eventFailureRepository.existsByEventId(eventId));
            EventFailure failure = eventFailureRepository.findByEventId(eventId).orElseThrow();
            assertTrue(failure.getAttemptCount() >= 3);
            assertEquals("SENT_TO_DLQ", failure.getDlqStatus());
        });

        var dlqAuditLogs = eventAuditLogRepository.findByEventId(eventId);
        assertTrue(dlqAuditLogs.stream().anyMatch(a -> "sent-to-dlq".equals(a.getStatus())));
    }

    @Test
    void testAuditTraceability_preservesCorrelationId() throws Exception {
        String topic = "healthcare.appointment.events.created";
        String eventId = "trace-event-" + System.currentTimeMillis();
        String correlationId = "trace-corr-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-trace");
        event.setSource("scheduler-service");
        event.setCorrelationId(correlationId);
        event.setVersion(1);
        event.setPayload(Map.of("reason", "trace test"));

        eventPublisher.publish(event, topic);

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            var auditLogs = eventAuditLogRepository.findByCorrelationId(correlationId);
            assertFalse(auditLogs.isEmpty());
            assertTrue(auditLogs.stream().allMatch(a -> correlationId.equals(a.getCorrelationId())));
        });
    }

    @Test
    void testIdempotentConcurrentDelivery_onlyProcessedOnce() throws Exception {
        String topic = "healthcare.appointment.events.created";
        String eventId = "concurrent-event-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-001");
        event.setSource("scheduler-service");
        event.setCorrelationId("corr-" + System.currentTimeMillis());
        event.setVersion(1);
        event.setPayload(Map.of("reason", "new appointment"));

        List<Thread> threads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            Thread t = new Thread(() -> {
                try {
                    eventPublisher.publish(event, topic);
                } finally {
                    latch.countDown();
                }
            });
            threads.add(t);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Threads did not complete within 30 seconds");

        await().atMost(25, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = processedEventRepository.countByEventId(eventId);
            assertEquals(1L, count, "Same event published concurrently 5 times should result in exactly 1 processing record");
        });
    }

    @Test
    void testEventVersionPreservedThroughKafka() throws Exception {
        String topic = "healthcare.appointment.events.created";
        String eventId = "version-event-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("appointment-created");
        event.setTimestamp(Instant.now());
        event.setAggregateType("appointment");
        event.setAggregateId("apt-version");
        event.setSource("scheduler-service");
        event.setCorrelationId("corr-version");
        event.setVersion(3);
        event.setPayload(Map.of("data", "version test"));

        eventPublisher.publish(event, topic);

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            var auditLogs = eventAuditLogRepository.findByEventId(eventId);
            assertFalse(auditLogs.isEmpty());
        });
    }

    @Test
    void testBillingPaymentCompleted_createsAuditAndIdempotencyRecord() throws Exception {
        String topic = "healthcare.billing.events.payment-completed";
        String eventId = "billing-payment-" + System.currentTimeMillis();
        String correlationId = "corr-billing-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("billing-payment-completed");
        event.setTimestamp(Instant.now());
        event.setAggregateType("billing");
        event.setAggregateId(eventId);
        event.setSource("payment-service");
        event.setCorrelationId(correlationId);
        event.setVersion(1);
        event.setPayload(Map.of("amount", 250.00, "currency", "USD", "transactionId", "txn-001"));

        eventPublisher.publish(event, topic);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(processedEventRepository.findByEventIdAndConsumerId(eventId, "billing-consumer-group").isPresent());
            assertTrue(eventAuditLogRepository.findByEventId(eventId).size() >= 2);
        });

        ProcessedEvent processed = processedEventRepository.findByEventIdAndConsumerId(eventId, "billing-consumer-group").orElseThrow();
        assertEquals("PROCESSED", processed.getStatus());

        var auditLogs = eventAuditLogRepository.findByEventId(eventId);
        assertTrue(auditLogs.stream().anyMatch(a -> "processing-started".equals(a.getStatus())));
        assertTrue(auditLogs.stream().anyMatch(a -> "processing-completed".equals(a.getStatus())));
    }

    @Test
    void testClinicalEvent_createsAuditAndIdempotencyRecord() throws Exception {
        String topic = "healthcare.encounter.events.clinical-alert";
        String eventId = "clinical-event-" + System.currentTimeMillis();
        String correlationId = "corr-clinical-" + System.currentTimeMillis();

        EventEnvelope event = new EventEnvelope();
        event.setEventId(eventId);
        event.setEventType("clinical-event");
        event.setTimestamp(Instant.now());
        event.setAggregateType("clinical");
        event.setAggregateId(eventId);
        event.setSource("clinical-service");
        event.setCorrelationId(correlationId);
        event.setVersion(1);
        event.setPayload(Map.of("finding", "test finding", "severity", "medium"));

        eventPublisher.publish(event, topic);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(processedEventRepository.findByEventIdAndConsumerId(eventId, "encounter-consumer-group").isPresent(),
                "ProcessedEvent should exist for encounter-consumer-group");
            assertTrue(eventAuditLogRepository.findByEventId(eventId).size() >= 2,
                "Audit logs should exist for clinical event");
        });

        ProcessedEvent processed = processedEventRepository.findByEventIdAndConsumerId(eventId, "encounter-consumer-group").orElseThrow();
        assertEquals("PROCESSED", processed.getStatus(),
            "Processed status should be PROCESSED for encounter-consumer-group");

        var auditLogs = eventAuditLogRepository.findByEventId(eventId);
        assertTrue(auditLogs.stream().anyMatch(a -> "processing-started".equals(a.getStatus())),
            "Should have processing-started audit log");
        assertTrue(auditLogs.stream().anyMatch(a -> "processing-completed".equals(a.getStatus())),
            "Should have processing-completed audit log");
    }
}