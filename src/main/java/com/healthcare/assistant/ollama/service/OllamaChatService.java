package com.healthcare.assistant.ollama.service;

import com.healthcare.assistant.ollama.OllamaProperties;
import com.healthcare.assistant.service.AiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.*;

/**
 * Calls a locally running Ollama model through its {@code /api/generate}
 * endpoint. The request body is sent as JSON
 * ({@code application/json}) because the Ollama REST API does not accept
 * {@code application/x-www-form-urlencoded} or multipart form bodies; sending
 * anything else results in HTTP 400 and no {@code response} field, which would
 * be silently masked by the safety fallback. Using a {@code LinkedHashMap}
 * (not a {@code MultiValueMap}) ensures {@code RestTemplate} selects the
 * {@code MappingJackson2HttpMessageConverter} via the explicit JSON content
 * type.
 */
@Service
public class OllamaChatService implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaChatService.class);

    private final RestTemplate restTemplate;
    private final OllamaProperties properties;

    public OllamaChatService(RestTemplate restTemplate, OllamaProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Sends a prompt to the Ollama model and returns the generated text.
     *
     * @param prompt user question or symptom description
     * @return generated text from the model or a deterministic safe fallback
     */
    @Override
    public String generateResponse(String prompt) {
        try {
            String url = properties.getBaseUrl() + "/api/generate";
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", properties.getModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = responseEntity.getBody();
                if (responseMap != null && responseMap.containsKey("response")) {
                    Object responseObj = responseMap.get("response");
                    if (responseObj instanceof String && !((String) responseObj).isBlank()) {
                        return (String) responseObj;
                    }
                }
            }
            log.warn("Ollama returned status {} without a usable 'response' field", responseEntity.getStatusCode());
            return "Safety override: AI service returned an unexpected response.";
        } catch (RestClientException e) {
            log.error("Ollama call failed: {}", e.getMessage());
            return "Safety override: AI service currently unavailable. Please consult a qualified healthcare professional.";
        }
    }
}