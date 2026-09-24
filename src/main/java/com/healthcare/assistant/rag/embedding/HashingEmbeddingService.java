package com.healthcare.assistant.rag.embedding;

import com.healthcare.assistant.rag.config.RagProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Deterministic, dependency-free embedding implementation used as the default
 * and in tests.
 * <p>
 * Tokens are lower-cased ASCII word fragments; each token is mapped through a
 * stable hash to a position in a fixed-dimension vector whose cell is updated
 * with a sign component derived from the token hash. The result is L2
 * normalised. The same text always produces the same vector, so retrieval is
 * deterministic and the test suite does not require an external embedding model.
 * <p>
 * This is intentionally simple but consistent: cosine similarity between two
 * texts grows with the overlap of their token sets, which is sufficient to
 * produce meaningful retrieval ordering for the patient-education flow and to
 * exercise the full RAG pipeline end-to-end without an external dependency.
 * Production deployments that need richer embeddings should switch the
 * {@code rag.embedding.provider} property to {@code ollama} (or another
 * provider) without touching the rest of the pipeline.
 */
@Component
@ConditionalOnProperty(prefix = "rag.embedding", name = "provider", havingValue = "hash", matchIfMissing = true)
public class HashingEmbeddingService implements EmbeddingService {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-z0-9]+");
    private static final int LARGE_PRIME = 0x811C9DC5;

    private final int dimension;

    public HashingEmbeddingService(RagProperties properties) {
        this.dimension = Math.max(32, properties.getEmbedding().getDimension());
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[dimension];
        }
        float[] vector = new float[dimension];
        String lower = text.toLowerCase(Locale.ROOT);
        for (String token : TOKEN_SPLIT.split(lower)) {
            if (token.isEmpty()) {
                continue;
            }
            int hash = hash(token);
            int idx = Math.floorMod(hash, dimension);
            int sign = ((hash >>> 31) & 1) == 1 ? 1 : -1;
            vector[idx] += sign;
        }
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm > 0) {
            double scale = 1.0 / Math.sqrt(norm);
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] * scale);
            }
        }
        return vector;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public String providerName() {
        return "hash";
    }

    private static int hash(String token) {
        int h = LARGE_PRIME;
        for (int i = 0; i < token.length(); i++) {
            h ^= token.charAt(i);
            h *= 0x01000193;
        }
        return h;
    }
}
