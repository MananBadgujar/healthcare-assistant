package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Embedding vector stored for a {@link KbChunk}.
 * <p>
 * The embedding is stored as a serialised string of space-separated floats. A
 * JPA-friendly representation is used so that the same schema works on MySQL and
 * the in-memory H2 test database without requiring a native vector column type.
 * Similarity is computed in Java by the {@code VectorSearchService} which keeps
 * the storage portable and avoids introducing additional infrastructure.
 * <p>
 * Keeping the embedding separate from the chunk allows re-embedding (regenerating
 * embeddings with a different model) without re-chunking or touching chunk text.
 */
@Entity
@Table(name = "kb_embeddings",
        indexes = @Index(name = "idx_kb_embeddings_chunk", columnList = "chunk_id"))
@Data
public class KbEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the chunk this embedding was generated for. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false)
    private KbChunk chunk;

    /** Name of the embedding model used to produce this vector. */
    @Column(name = "embedding_model", nullable = false)
    private String embeddingModel;

    /** Dimensionality of the embedding vector. */
    @Column(nullable = false)
    private int dimension;

    /** Space-separated float values, e.g. "0.12 -0.03 0.45 ...". */
    @Column(name = "vector", nullable = false)
    @Lob
    private String vector;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
