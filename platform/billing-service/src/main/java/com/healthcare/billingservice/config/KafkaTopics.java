package com.healthcare.billingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopics {
    @Bean public NewTopic payCompleted() { return new NewTopic("healthcare.billing.events.payment-completed", 1, (short) 1); }
    @Bean public NewTopic claimSubmitted() { return new NewTopic("healthcare.claim.events.submitted", 1, (short) 1); }
}
