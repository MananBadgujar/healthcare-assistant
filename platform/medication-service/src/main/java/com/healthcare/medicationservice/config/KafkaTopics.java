package com.healthcare.medicationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic medCreated() { return new NewTopic("healthcare.medication.events.created", 1, (short) 1); }
    @Bean public NewTopic medRefill() { return new NewTopic("healthcare.medication.events.refill-requested", 1, (short) 1); }
}
