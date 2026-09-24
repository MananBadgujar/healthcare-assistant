package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.KnowledgeBaseArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KnowledgeBaseArticleRepository extends JpaRepository<KnowledgeBaseArticle, Long> {
    Optional<KnowledgeBaseArticle> findByTitle(String title);
}