package com.healthcare.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic lowStock() { return new NewTopic("healthcare.inventory.events.low-stock", 1, (short) 1); }
    @Bean public NewTopic nearExpiry() { return new NewTopic("healthcare.inventory.events.near-expiry", 1, (short) 1); }
}
