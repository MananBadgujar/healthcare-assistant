package com.healthcare.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EntityScan(basePackages = {"com.healthcare.assistant.entity", "com.healthcare.assistant.kafka.entity"})
public class HealthcareAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthcareAssistantApplication.class, args);
    }
}