package com.healthcare.authservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic authRegistered() {
        return new NewTopic("healthcare.auth.events.registered", 1, (short) 1);
    }
}
