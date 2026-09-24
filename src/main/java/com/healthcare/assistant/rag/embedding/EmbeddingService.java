package com.healthcare.assistant.rag.embedding;

/**
 * Abstraction over the operation that turns text into a vector.
 * <p>
 * Implementations are pluggable via {@link EmbeddingProvider} beans and selected
 * at runtime by the configured provider name (see {@code rag.embedding.provider}).
 * Keeping the embedding step behind an interface prevents the rest of the RAG
 * system from being coupled to a single embedding model or vendor.
 */
public interface EmbeddingService {

    /**
     * Generate an embedding for the supplied text.
     *
     * @param text text to embed, non-blank
     * @return float vector of length {@link #dimension()}
     * @throws EmbeddingException when the provider call fails
     */
    float[] embed(String text);

    /**
     * Dimensionality of vectors produced by this service.
     *
     * @return positive integer
     */
    int dimension();

    /**
     * Name of the backing provider, used by the selector to route traffic.
     *
     * @return non-blank provider name
     */
    String providerName();
}
