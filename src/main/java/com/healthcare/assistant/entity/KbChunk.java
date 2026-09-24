package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A chunk of a {@link KbDocument} produced during document processing.
 * <p>
 * Each chunk keeps enough metadata to trace it back to its origin document and
 * version, to support citation generation and stale-content filtering. The chunk
 * text is stored verbatim so that it can be re-embedded or re-indexed without
 * re-processing the source document.
 */
@Entity
@Table(name = "kb_chunks",
        indexes = {
                @Index(name = "idx_kb_chunks_doc", columnList = "document_id"),
                @Index(name = "idx_kb_chunks_active", columnList = "active")
        })
@Data
public class KbChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable unique chunk identifier, e.g. "{externalId}-v{version}-c{sequence}". */
    @Column(name = "chunk_id", nullable = false, unique = true)
    private String chunkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KbDocument document;

    @Column(name = "chunk_text", nullable = false)
    @Lob
    private String chunkText;

    /** Sequence number of the chunk within the parent document (0-based). */
    @Column(nullable = false)
    private Integer sequence;

    /** Logical section the chunk belongs to, when known (e.g. "Causes"). */
    private String section;

    /** Page number when the source format has pages (PDF), otherwise null. */
    private Integer page;

    /** Whether this chunk participates in retrieval. Mirrors document active flag. */
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
