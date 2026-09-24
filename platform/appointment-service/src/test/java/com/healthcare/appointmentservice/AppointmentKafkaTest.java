package com.healthcare.appointmentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.DomainEvent;
import com.healthcare.contracts.Topics;
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
@EmbeddedKafka(partitions = 1, topics = {Topics.APPOINTMENT_CREATED})
public class AppointmentKafkaTest {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ObjectMapper om;
    @Autowired EmbeddedKafkaBroker embeddedKafka;

    @Test
    public void producerDeliversToConsumer() throws Exception {
        Map<String, Object> cProps = KafkaTestUtils.consumerProps(
                "phase13-test-" + System.nanoTime(), "true", embeddedKafka);
        cProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> cf =
                new DefaultKafkaConsumerFactory<>(cProps, new StringDeserializer(), new StringDeserializer());
        try (Consumer<String, String> consumer = cf.createConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, Topics.APPOINTMENT_CREATED);
            DomainEvent e = DomainEvent.of("appointment.created", "Appointment", "7",
                    "appointment-service", "corr-test", Map.of("status", "PENDING"));
            kafka.send(Topics.APPOINTMENT_CREATED, "7", om.writeValueAsString(e)).get();
            ConsumerRecord<String, String> rec =
                    KafkaTestUtils.getSingleRecord(consumer, Topics.APPOINTMENT_CREATED, java.time.Duration.ofSeconds(20));
            assertNotNull(rec, "consumer should receive the event");
            DomainEvent back = om.readValue(rec.value(), DomainEvent.class);
            assertEquals("7", back.getEntityId());
            assertEquals("corr-test", back.getCorrelationId());
            assertEquals("v1", back.getEventVersion());
        }
    }
}
