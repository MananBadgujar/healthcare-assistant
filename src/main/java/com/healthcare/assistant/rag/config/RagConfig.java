package com.healthcare.assistant.rag.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the {@link RagProperties} configuration properties.
 */
@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig {
}
