package com.healthcare.appointmentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic apptCreated() { return new NewTopic("healthcare.appointment.events.created", 1, (short) 1); }
    @Bean public NewTopic apptCancelled() { return new NewTopic("healthcare.appointment.events.cancelled", 1, (short) 1); }
    @Bean public NewTopic apptStatus() { return new NewTopic("healthcare.appointment.events.status-changed", 1, (short) 1); }
}
