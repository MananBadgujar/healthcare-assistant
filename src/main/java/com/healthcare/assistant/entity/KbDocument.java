package com.healthcare.assistant.entity;

import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Knowledge-base document entity.
 * <p>
 * A {@code KbDocument} represents a controlled, versioned piece of healthcare
 * educational content that can be ingested, chunked, embedded and used as the
 * retrieval source for the RAG patient-education flow. Metadata required by the
 * RAG pipeline (source, category, topic, version, active status, publication
 * and update dates) is stored directly on the document so that retrieved
 * chunks can always be traced back to their origin and version.
 * <p>
 * Versioning is implemented by treating each version as a separate row; the
 * combination of {@code externalId} + {@code version} uniquely identifies a
 * versioned document. Activation/deactivation of versions is controlled by the
 * {@code active} flag; inactive versions are excluded from retrieval.
 */
@Entity
@Table(name = "kb_documents",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kb_documents_external_id_version",
                columnNames = {"external_id", "version"}))
@Data
public class KbDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable identifier shared across all versions of the same document. */
    @Column(name = "external_id", nullable = false)
    private String externalId;

    /** Human-friendly document name / title. */
    @Column(nullable = false)
    private String name;

    /** Free-text description of where the content originated. */
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KbCategory category;

    /** Specific topic within the category (e.g. "Type 2 Diabetes"). */
    private String topic;

    /** Monotonically increasing version label, e.g. "1", "1.1", "2". */
    @Column(nullable = false)
    private String version;

    /** Original format of the ingested content. */
    @Enumerated(EnumType.STRING)
    private KbDocumentFormat format;

    /** Original content (text/markdown/PDF extracted text) for re-processing. */
    @Lob
    private String content;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "update_date")
    private LocalDate updateDate;

    /**
     * Whether this version should be considered during retrieval. Only one
     * version of a given {@code externalId} should normally be active at a time;
     * this is enforced by the admin service rather than the database to keep
     * schema portable across MySQL and H2.
     */
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
