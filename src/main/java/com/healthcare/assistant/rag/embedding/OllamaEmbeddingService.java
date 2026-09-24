package com.healthcare.assistant.rag.embedding;

import com.healthcare.assistant.ollama.OllamaProperties;
import com.healthcare.assistant.rag.config.RagProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedding implementation that delegates to the Ollama {@code /api/embeddings}
 * endpoint.
 * <p>
 * Only activated when {@code rag.embedding.provider=ollama} is set, so the
 * pipeline runs without an external dependency by default. On any HTTP error or
 * timeout this implementation throws {@link EmbeddingException}; the caller is
 * responsible for falling back (typically the ingestion service logs and skips,
 * and the retrieval service falls back to the configured safe message).
 */
@Component
@ConditionalOnProperty(prefix = "rag.embedding", name = "provider", havingValue = "ollama")
public class OllamaEmbeddingService implements EmbeddingService {

    private final RestTemplate restTemplate;
    private final OllamaProperties ollamaProperties;
    private final String modelName;

    public OllamaEmbeddingService(RestTemplate restTemplate,
                                  OllamaProperties ollamaProperties,
                                  RagProperties ragProperties) {
        this.restTemplate = restTemplate;
        this.ollamaProperties = ollamaProperties;
        this.modelName = ragProperties.getEmbedding().getOllamaModel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new EmbeddingException("Cannot embed empty text");
        }
        String url = ollamaProperties.getBaseUrl() + "/api/embeddings";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelName);
        body.put("prompt", text);
        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response == null || !response.containsKey("embedding")) {
                throw new EmbeddingException("Ollama embeddings response missing 'embedding'");
            }
            Object embeddingObj = response.get("embedding");
            if (!(embeddingObj instanceof List)) {
                throw new EmbeddingException("Ollama embeddings 'embedding' is not a list");
            }
            List<? extends Number> values = (List<? extends Number>) embeddingObj;
            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = values.get(i).floatValue();
            }
            return vector;
        } catch (RestClientException e) {
            throw new EmbeddingException("Ollama embeddings call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int dimension() {
        // Ollama embedding size is model-dependent; dimension is inferred from
        // the first embedding rather than fixed here.
        return 0;
    }

    @Override
    public String providerName() {
        return "ollama";
    }
}
