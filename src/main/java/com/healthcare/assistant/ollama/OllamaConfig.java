package com.healthcare.assistant.ollama;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Ollama AI client.
 */
@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}