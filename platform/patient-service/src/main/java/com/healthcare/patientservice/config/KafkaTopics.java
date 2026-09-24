package com.healthcare.patientservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic patientCreated() { return new NewTopic("healthcare.patient.events.created", 1, (short) 1); }
    @Bean public NewTopic patientUpdated() { return new NewTopic("healthcare.patient.events.updated", 1, (short) 1); }
}
