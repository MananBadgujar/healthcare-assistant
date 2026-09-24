package com.healthcare.assistant.controller;

import com.healthcare.assistant.rag.PatientEducationFacade;
import com.healthcare.assistant.rag.dto.AskRequest;
import com.healthcare.assistant.rag.dto.RagResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public patient-education ask endpoint. Replaces the legacy stub with the
 * Phase 9 RAG pipeline: safety guard -> retrieval -> LLM -> citation
 * validation -> structured response. The endpoint is JWT-authenticated by the
 * global security configuration.
 */
@RestController
@RequestMapping("/api/v1/kb/ask")
public class KnowledgeBaseAskController {

    private final PatientEducationFacade educationFacade;

    public KnowledgeBaseAskController(PatientEducationFacade educationFacade) {
        this.educationFacade = educationFacade;
    }

    @PostMapping
    public ResponseEntity<RagResponse> askQuestion(@Valid @RequestBody AskRequest request) {
        RagResponse response = educationFacade.ask(request);
        return ResponseEntity.ok(response);
    }
}
