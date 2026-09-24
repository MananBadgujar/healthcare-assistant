package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.KbArticleCreateRequest;
import com.healthcare.assistant.entity.KnowledgeBaseArticle;
import com.healthcare.assistant.service.KnowledgeBaseArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Legacy knowledge-base article endpoint retained for backwards compatibility.
 * <p>
 * This controller and the underlying {@link KnowledgeBaseArticle} entity are
 * <strong>not</strong> part of the Phase 9 RAG patient-education pipeline.
 * Phase 9 knowledge-base content is managed via
 * {@code /api/v1/kb/admin/documents} (structured JSON ingest or PDF/TXT/MD
 * upload) and served through {@code /api/v1/kb/search} and
 * {@code /api/v1/kb/ask}.
 * <p>
 * The endpoint remains live to avoid breaking older clients but new content
 * must be ingested through the Phase 9 admin endpoints so it participates in
 * the retrieval, citation and safety flow.
 *
 * @deprecated since Phase 9; use {@code /api/v1/kb/admin/documents} instead.
 */
@Deprecated
@RestController
@RequestMapping("/api/v1/kb/articles")
public class KnowledgeBaseArticleController {

    private final KnowledgeBaseArticleService articleService;

    @Autowired
    public KnowledgeBaseArticleController(KnowledgeBaseArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeBaseArticle> createArticle(@RequestBody KbArticleCreateRequest request) {
        KnowledgeBaseArticle article = new KnowledgeBaseArticle(request.getTitle(), request.getContent());
        KnowledgeBaseArticle saved = articleService.saveArticle(article);
        return ResponseEntity.ok(saved);
    }
}