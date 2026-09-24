package com.healthcare.aiservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic triageDone() { return new NewTopic("healthcare.ai.events.triage-completed", 1, (short) 1); }
}
