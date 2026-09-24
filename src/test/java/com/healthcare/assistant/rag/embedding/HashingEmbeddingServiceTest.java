package com.healthcare.assistant.rag.embedding;

import com.healthcare.assistant.rag.config.RagProperties;
import com.healthcare.assistant.rag.vector.VectorMath;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HashingEmbeddingService}. Verifies determinism (the
 * same text always produces the same vector), dimension invariants and that
 * semantically-related texts score higher than unrelated ones.
 */
class HashingEmbeddingServiceTest {

    private HashingEmbeddingService serviceWithDimension(int dim) {
        RagProperties properties = new RagProperties();
        properties.getEmbedding().setDimension(dim);
        return new HashingEmbeddingService(properties);
    }

    @Test
    void embedIsDeterministic() {
        HashingEmbeddingService service = serviceWithDimension(64);
        float[] a = service.embed("Type 2 diabetes is a chronic condition");
        float[] b = service.embed("Type 2 diabetes is a chronic condition");
        assertArrayEquals(a, b, 1e-6f);
    }

    @Test
    void dimensionMatchesConfiguration() {
        assertEquals(128, serviceWithDimension(128).dimension());
        assertEquals(64, serviceWithDimension(64).dimension());
    }

    @Test
    void blankTextReturnsZeroVector() {
        HashingEmbeddingService service = serviceWithDimension(64);
        float[] vector = service.embed("   ");
        assertEquals(64, vector.length);
        for (float v : vector) {
            assertEquals(0f, v, 0f);
        }
    }

    @Test
    void relatedTextsHaveHigherSimilarityThanUnrelated() {
        HashingEmbeddingService service = serviceWithDimension(256);
        float[] diabetes = service.embed("Type 2 diabetes chronic condition blood glucose management");
        float[] diabetesAgain = service.embed("Type 2 diabetes blood glucose chronic condition management");
        float[] weather = service.embed("Today's weather forecast includes rain and thunderstorms");

        double related = VectorMath.cosineSimilarity(diabetes, diabetesAgain);
        double unrelated = VectorMath.cosineSimilarity(diabetes, weather);
        assertTrue(related > unrelated,
                "related texts should be more similar than unrelated; related=" + related
                        + " unrelated=" + unrelated);
        assertTrue(related > 0.0, "related texts should have positive similarity");
    }

    @Test
    void dimensionIsAlwaysLowerBounded() {
        HashingEmbeddingService service = serviceWithDimension(8); // below the 32 floor
        assertTrue(service.dimension() >= 32);
    }

    @Test
    void vectorMathRoundTripPreservesValues() {
        HashingEmbeddingService service = serviceWithDimension(64);
        float[] vector = service.embed("medical education about insulin and glucose");
        String encoded = VectorMath.encode(vector);
        float[] decoded = VectorMath.decode(encoded);
        assertEquals(vector.length, decoded.length);
        for (int i = 0; i < vector.length; i++) {
            assertEquals(vector[i], decoded[i], 1e-5f);
        }
    }

    @Test
    void cosineSimilarityNullOrZeroReturnsZero() {
        assertEquals(0.0, VectorMath.cosineSimilarity(null, new float[]{1f}));
        assertEquals(0.0, VectorMath.cosineSimilarity(new float[]{0f}, new float[]{0f}));
        assertEquals(0.0, VectorMath.cosineSimilarity(new float[]{1f, 2f}, new float[]{1f}));
    }
}
