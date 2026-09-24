package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.KbDocument;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.rag.dto.DocumentMetadataDto;
import com.healthcare.assistant.rag.dto.IngestStructuredRequest;
import com.healthcare.assistant.rag.ingestion.DocumentIngestionService;
import com.healthcare.assistant.rag.ingestion.ExtractionException;
import com.healthcare.assistant.rag.ingestion.IngestDocumentRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST endpoints for the healthcare knowledge base.
 * <p>
 * All endpoints require the {@code ADMIN} (or {@code PROVIDER}) role; patient
 * users are forbidden via {@code @PreAuthorize}. The endpoints allow uploading
 * structured content or binary documents (PDF/TXT), patching metadata,
 * activating/deactivating versions, re-indexing and regenerating embeddings, and
 * listing documents and categories.
 */
@RestController
@RequestMapping("/api/v1/kb/admin")
@PreAuthorize("hasAnyRole('ADMIN','PROVIDER')")
public class KnowledgeBaseAdminController {

    private final DocumentIngestionService ingestionService;

    public KnowledgeBaseAdminController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /** List supported knowledge-base categories. */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(ingestionService.supportedCategories());
    }

    /** List all documents with their metadata. */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentMetadataDto>> documents() {
        return ResponseEntity.ok(ingestionService.findAll().stream()
                .map(DocumentMetadataDto::new)
                .collect(Collectors.toList()));
    }

    /** List all versions of a given external id. */
    @GetMapping("/documents/{externalId}/versions")
    public ResponseEntity<List<DocumentMetadataDto>> versions(@PathVariable String externalId) {
        return ResponseEntity.ok(ingestionService.findVersions(externalId).stream()
                .map(DocumentMetadataDto::new)
                .collect(Collectors.toList()));
    }

    /** Get a single document's metadata. */
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<DocumentMetadataDto> document(@PathVariable Long documentId) {
        return ResponseEntity.ok(new DocumentMetadataDto(ingestionService.findDocumentById(documentId)));
    }

    /** Create a structured-text document (content provided inline). */
    @PostMapping(path = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> ingestStructured(@RequestBody IngestStructuredRequest request) {
        IngestDocumentRequest ingestRequest = toIngest(request);
        KbDocument saved = ingestionService.ingest(ingestRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentMetadataDto(saved));
    }

    /** Upload a binary document (PDF/TXT/Markdown) with metadata. */
    @PostMapping(path = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam("externalId") String externalId,
                                    @RequestParam("name") String name,
                                    @RequestParam("category") KbCategory category,
                                    @RequestParam(value = "topic", required = false) String topic,
                                    @RequestParam(value = "version", required = false) String version,
                                    @RequestParam(value = "source", required = false) String source)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        IngestDocumentRequest request = new IngestDocumentRequest();
        request.setExternalId(externalId);
        request.setName(name);
        request.setCategory(category);
        request.setTopic(topic);
        request.setVersion(version);
        request.setSource(source);
        request.setFormat(detectFormat(file));
        request.setPayload(file.getBytes());
        KbDocument saved = ingestionService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentMetadataDto(saved));
    }

    /** Re-index an existing document (re-chunk + re-embed). */
    @PostMapping("/documents/{documentId}/reindex")
    public ResponseEntity<?> reindex(@PathVariable Long documentId) {
        return ResponseEntity.ok(new DocumentMetadataDto(ingestionService.reindex(documentId)));
    }

    /** Regenerate embeddings for an existing document without re-chunking. */
    @PostMapping("/documents/{documentId}/embeddings/regenerate")
    public ResponseEntity<?> regenerateEmbeddings(@PathVariable Long documentId) {
        ingestionService.regenerateEmbeddings(documentId);
        return ResponseEntity.noContent().build();
    }

    /** Activate a version of a document; all other versions become inactive. */
    @PostMapping("/documents/{externalId}/activate/{version}")
    public ResponseEntity<?> activate(@PathVariable String externalId, @PathVariable String version) {
        KbDocument doc = ingestionService.activateVersion(externalId, version);
        return ResponseEntity.ok(new DocumentMetadataDto(doc));
    }

    /** Deactivate a document (excludes its chunks from retrieval). */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<?> deactivate(@PathVariable Long documentId) {
        return ResponseEntity.ok(new DocumentMetadataDto(ingestionService.deactivate(documentId)));
    }

    private IngestDocumentRequest toIngest(IngestStructuredRequest request) {
        IngestDocumentRequest ingest = new IngestDocumentRequest();
        ingest.setExternalId(request.getExternalId());
        ingest.setName(request.getName());
        ingest.setCategory(request.getCategory());
        ingest.setTopic(request.getTopic());
        ingest.setVersion(request.getVersion());
        ingest.setSource(request.getSource());
        ingest.setFormat(request.getFormat());
        ingest.setExplicitText(request.getContent());
        ingest.setPublicationDate(request.getPublicationDate());
        ingest.setUpdateDate(request.getUpdateDate());
        return ingest;
    }

    private com.healthcare.assistant.entity.enums.KbDocumentFormat detectFormat(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (original.endsWith(".pdf")) {
            return com.healthcare.assistant.entity.enums.KbDocumentFormat.PDF;
        }
        if (original.endsWith(".md") || original.endsWith(".markdown")) {
            return com.healthcare.assistant.entity.enums.KbDocumentFormat.MARKDOWN;
        }
        return com.healthcare.assistant.entity.enums.KbDocumentFormat.TXT;
    }

    @ExceptionHandler(ExtractionException.class)
    public ResponseEntity<Map<String, String>> handleExtraction(ExtractionException ex) {
        LoggerFactory.getLogger(KnowledgeBaseAdminController.class).warn("Ingestion failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
