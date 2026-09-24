package com.healthcare.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.notificationservice.repo.NotificationRepository;
import com.healthcare.notificationservice.service.NotificationConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"healthcare.appointment.events.created"})
public class NotificationKafkaTest {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ObjectMapper om;
    @Autowired NotificationConsumer consumer;
    @Autowired NotificationRepository repo;
    @Autowired EmbeddedKafkaBroker embeddedKafka;

    @Test
    public void consumerIsIdempotent() throws Exception {
        DomainEvent e = DomainEvent.of("appointment.created", "Appointment", "9",
                "appointment-service", "corr-n", Map.of());
        String raw = om.writeValueAsString(e);
        // Prove the event really travels through Kafka first.
        Map<String, Object> cProps = KafkaTestUtils.consumerProps(
                "phase13-notif-test-" + System.nanoTime(), "true", embeddedKafka);
        cProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> cf =
                new DefaultKafkaConsumerFactory<>(cProps, new StringDeserializer(), new StringDeserializer());
        try (Consumer<String, String> probe = cf.createConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(probe, "healthcare.appointment.events.created");
            kafka.send("healthcare.appointment.events.created", "9", raw).get();
            ConsumerRecord<String, String> rec = KafkaTestUtils.getSingleRecord(
                    probe, "healthcare.appointment.events.created", java.time.Duration.ofSeconds(20));
            assertNotNull(rec, "event must reach kafka");
        }
        // Feed the consumed payload through the real consumer twice -> idempotent.
        consumer.onEvent(raw);
        consumer.onEvent(raw); // duplicate delivery
        assertEquals(1, repo.findAll().size(), "duplicate event must not create a second notification");
    }
}
