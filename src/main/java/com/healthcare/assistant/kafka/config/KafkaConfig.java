package com.healthcare.assistant.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.ExponentialBackOff;

import com.healthcare.assistant.kafka.exception.RetryableException;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${security.sasl.username:}")
    private String saslUsername;

    @Value("${security.sasl.password:}")
    private String saslPassword;

    @Value("${security.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        if (sslEnabled) {
            configProps.put("security.protocol", "SSL");
            configProps.put("ssl.truststore.location", "classpath:truststore.jks");
            configProps.put("ssl.keystore.location", "classpath:keystore.jks");
            configProps.put("ssl.key.password", "changeit");
            configProps.put("ssl.truststore.password", "changeit");
            configProps.put("ssl.keystore.password", "changeit");
        }
        if (saslUsername != null && !saslUsername.isEmpty()) {
            configProps.put("sasl.mechanism", "SCRAM-SHA-256");
            configProps.put("security.protocol", "SASL_PLAINTEXT");
            configProps.put("sasl.username", saslUsername);
            configProps.put("sasl.password", saslPassword);
        }
        return new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, byte[]> byteArrayProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        if (sslEnabled) {
            configProps.put("security.protocol", "SSL");
            configProps.put("ssl.truststore.location", "classpath:truststore.jks");
            configProps.put("ssl.keystore.location", "classpath:keystore.jks");
            configProps.put("ssl.key.password", "changeit");
            configProps.put("ssl.truststore.password", "changeit");
            configProps.put("ssl.keystore.password", "changeit");
        }
        if (saslUsername != null && !saslUsername.isEmpty()) {
            configProps.put("sasl.mechanism", "SCRAM-SHA-256");
            configProps.put("security.protocol", "SASL_PLAINTEXT");
            configProps.put("sasl.username", saslUsername);
            configProps.put("sasl.password", saslPassword);
        }
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, byte[]> byteArrayKafkaTemplate(ProducerFactory<String, byte[]> byteArrayProducerFactory) {
        return new KafkaTemplate<>(byteArrayProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "healthcare-consumer-group");
        configProps.put("key.deserializer", (Object) StringDeserializer.class);
        configProps.put("value.deserializer", (Object) StringDeserializer.class);
        // Match application-kafka.properties (spring.kafka.consumer.auto-offset-reset=earliest):
        // this factory is hand-rolled so Boot's consumer properties don't bind automatically.
        // 'earliest' ensures a fresh consumer group doesn't skip events published before its first join.
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        if (sslEnabled) {
            configProps.put("security.protocol", "SSL");
            configProps.put("ssl.truststore.location", "classpath:truststore.jks");
            configProps.put("ssl.keystore.location", "classpath:keystore.jks");
            configProps.put("ssl.key.password", "changeit");
            configProps.put("ssl.truststore.password", "changeit");
            configProps.put("ssl.keystore.password", "changeit");
        }
        if (saslUsername != null && !saslUsername.isEmpty()) {
            configProps.put("sasl.mechanism", "SCRAM-SHA-256");
            configProps.put("security.protocol", "SASL_PLAINTEXT");
            configProps.put("sasl.username", saslUsername);
            configProps.put("sasl.password", saslPassword);
        }
        return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
            com.healthcare.assistant.kafka.retry.EventFailureRecorder failureRecorder,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + ".dlq", record.partition()));
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(4000);
        // Bound total retry time so exhausted failures reach the DLQ recoverer
        // (default maxElapsedTime is effectively infinite and never recovers).
        backOff.setMaxElapsedTime(20000L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, ex) -> {
            try {
                recoverer.accept(record, ex);
            } catch (Exception publishEx) {
                org.slf4j.LoggerFactory.getLogger(KafkaConfig.class)
                        .warn("DLQ publish failed for {}: {}", record.topic(), publishEx.getMessage());
            }
            java.util.Optional<HeldOrEvent> toPersist = EventEnvelopeHolder.holder()
                    .map(held -> new HeldOrEvent(held.event(), held.attempts()))
                    .or(() -> deserializeEnvelope(record, objectMapper).map(event -> new HeldOrEvent(event, 1)));
            toPersist.ifPresentOrElse(held -> {
                failureRecorder.recordSentToDlq(held.event(), record.topic(), held.attempts(), ex);
            }, () -> org.slf4j.LoggerFactory.getLogger(KafkaConfig.class)
                    .warn("DLQ recovery without deserialized event for topic {}", record.topic()));
            EventEnvelopeHolder.clear();
        }, backOff);
        errorHandler.addRetryableExceptions(RetryableException.class);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            deserializeEnvelope(record, objectMapper).ifPresent(event -> {
                EventEnvelopeHolder.set(event, deliveryAttempt);
                try {
                    failureRecorder.recordRetryAttempt(event, record.topic(), deliveryAttempt, ex);
                } catch (Exception persistEx) {
                    org.slf4j.LoggerFactory.getLogger(KafkaConfig.class)
                            .warn("Failed to persist retry attempt for {}: {}", event.getEventId(), persistEx.getMessage());
                }
            });
        });
        return errorHandler;
    }

    private static java.util.Optional<com.healthcare.assistant.kafka.event.EventEnvelope> deserializeEnvelope(
            org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        try {
            Object value = record.value();
            String json = value instanceof byte[] ? new String((byte[]) value) : String.valueOf(value);
            return java.util.Optional.of(objectMapper.readValue(json, com.healthcare.assistant.kafka.event.EventEnvelope.class));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(KafkaConfig.class)
                    .warn("Failed to deserialize failed record for failure persistence: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    /**
     * Holds the last deserialized failing event on the consumer thread so the
     * DLQ recoverer (which only receives the raw record) can persist the
     * existing {@code EventFailure} entity with full event context.
     */
    record HeldOrEvent(com.healthcare.assistant.kafka.event.EventEnvelope event, int attempts) {
    }

    static final class EventEnvelopeHolder {
        record Held(com.healthcare.assistant.kafka.event.EventEnvelope event, int attempts) {
        }

        private static final ThreadLocal<Held> HOLDER = new ThreadLocal<>();

        static void set(com.healthcare.assistant.kafka.event.EventEnvelope event, int attempts) {
            HOLDER.set(new Held(event, attempts));
        }

        static java.util.Optional<Held> holder() {
            return java.util.Optional.ofNullable(HOLDER.get());
        }

        static void clear() {
            HOLDER.remove();
        }
    }
}