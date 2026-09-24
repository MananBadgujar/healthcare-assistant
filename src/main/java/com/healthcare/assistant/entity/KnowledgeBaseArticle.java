package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Legacy knowledge-base article entity. <strong>Not</strong> part of the
 * Phase 9 RAG patient-education pipeline. Retained for backwards compatibility
 * with the legacy {@code /api/v1/kb/articles} endpoint; new content must be
 * ingested through {@code /api/v1/kb/admin/documents} instead.
 *
 * @deprecated since Phase 9; use {@link KbDocument} via the admin endpoint.
 */
@Deprecated
@Entity
@Table(name = "knowledge_base_article")
@Data
public class KnowledgeBaseArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Lob
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeBaseArticle() {}

    public KnowledgeBaseArticle(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}