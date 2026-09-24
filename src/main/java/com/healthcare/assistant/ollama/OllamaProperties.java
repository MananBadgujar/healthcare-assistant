package com.healthcare.assistant.ollama;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Ollama AI integration.
 * Binds to {@code ai.ollama.*} properties from {@code application.properties}.
 */
@Validated
@ConfigurationProperties(prefix = "ai.ollama")
public class OllamaProperties {
    /**
     * Base URL of the Ollama server (e.g., https://localhost:11433).
     */
    private String baseUrl;

    /**
     * API key used for authentication (if required by the deployment).
     */
    private String apiKey;

    /**
     * Name of the model to be used for completions.
     */
    private String model;

    /**
     * Timeout for HTTP requests to the Ollama service.
     */
    private String timeout;

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
}