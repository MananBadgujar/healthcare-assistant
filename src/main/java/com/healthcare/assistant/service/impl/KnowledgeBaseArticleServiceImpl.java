package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.KnowledgeBaseArticle;
import com.healthcare.assistant.repository.KnowledgeBaseArticleRepository;
import com.healthcare.assistant.service.KnowledgeBaseArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseArticleServiceImpl implements KnowledgeBaseArticleService {

    private final KnowledgeBaseArticleRepository repository;

    @Autowired
    public KnowledgeBaseArticleServiceImpl(KnowledgeBaseArticleRepository repository) {
        this.repository = repository;
    }

    @Override
    public KnowledgeBaseArticle saveArticle(KnowledgeBaseArticle article) {
        return repository.save(article);
    }
}