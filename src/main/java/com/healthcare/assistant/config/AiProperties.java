package com.healthcare.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for AI integration.
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * Enable/disable AI functionality.
     */
    private boolean enabled = false;

    /**
     * Base URL for Ollama service.
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * Default model to use if not overridden per request.
     */
    private String defaultModel = "vicuna";

    /**
     * Request timeout in milliseconds.
     */
    private int timeout = 30000;

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}