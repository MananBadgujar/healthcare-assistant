package com.healthcare.assistant.controller;

import com.healthcare.assistant.rag.dto.SearchHitDto;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.vector.VectorRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Public semantic-search endpoint: returns the relevant knowledge-base chunks
 * for a query without invoking the LLM. Useful for retrieving raw source
 * evidence explicitly or for evaluation. JWT-authenticated by the global
 * security configuration.
 */
@RestController
@RequestMapping("/api/v1/kb/search")
public class KnowledgeBaseSearchController {

    private final SemanticSearchService searchService;

    public KnowledgeBaseSearchController(SemanticSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<SearchHitDto>> search(@RequestParam String query,
                                                     @RequestParam(required = false) String category) {
        List<VectorRecord> hits = searchService.search(query, category);
        List<SearchHitDto> dtos = hits.stream()
                .map(record -> new SearchHitDto(record, 280))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
