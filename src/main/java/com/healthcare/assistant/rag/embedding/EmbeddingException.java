package com.healthcare.assistant.rag.embedding;

/**
 * Raised when an embedding cannot be generated. Surfaced as a typed exception
 * so the ingestion / retrieval flows can degrade to a safe fallback rather than
 * letting a provider error crash the pipeline.
 */
public class EmbeddingException extends RuntimeException {

    public EmbeddingException(String message) {
        super(message);
    }

    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
