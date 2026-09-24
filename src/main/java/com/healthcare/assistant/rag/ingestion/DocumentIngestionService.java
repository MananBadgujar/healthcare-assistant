package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.entity.KbChunk;
import com.healthcare.assistant.entity.KbDocument;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import com.healthcare.assistant.repository.KbChunkRepository;
import com.healthcare.assistant.rag.config.RagProperties;
import com.healthcare.assistant.rag.vector.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Document ingestion service: turns a raw upload request into a versioned
 * {@link KbDocument}, chunks the extracted text, persists {@link KbChunk}s and
 * generates + stores embeddings for each chunk. The service also handles
 * re-indexing (re-chunk + re-embed without modifying source content) and
 * activation/deactivation of document versions.
 * <p>
 * All failures raise typed exceptions from the {@code rag.ingestion} package so
 * the controller layer can translate them into clean HTTP responses without
 * leaking internal stack traces.
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final KbDocumentStore documentStore;
    private final KbChunkRepository chunkRepository;
    private final TextExtractorRegistry extractorRegistry;
    private final DocumentChunker chunker;
    private final VectorStore vectorStore;
    private final RagProperties properties;

    public DocumentIngestionService(KbDocumentStore documentStore,
                                    KbChunkRepository chunkRepository,
                                    TextExtractorRegistry extractorRegistry,
                                    DocumentChunker chunker,
                                    VectorStore vectorStore,
                                    RagProperties properties) {
        this.documentStore = documentStore;
        this.chunkRepository = chunkRepository;
        this.extractorRegistry = extractorRegistry;
        this.chunker = chunker;
        this.vectorStore = vectorStore;
        this.properties = properties;
    }

    /**
     * Ingest a new or versioned document.
     *
     * @param request document creation request (validated by caller)
     * @return the persisted document
     */
    @Transactional
    public KbDocument ingest(IngestDocumentRequest request) {
        validate(request);
        KbDocumentFormat format = request.getFormat();
        TextExtractor extractor = extractorRegistry.resolve(format);
        String extracted = extractor.extract(request.getPayload(), request.getExplicitText());
        if (extracted.isBlank()) {
            throw new ExtractionException("Document contained no extractable text");
        }
        if (extracted.length() > properties.getIngestion().getMaxDocumentChars()) {
            throw new ExtractionException("Document too large after extraction: "
                    + extracted.length() + " chars (max "
                    + properties.getIngestion().getMaxDocumentChars() + ")");
        }
        String version = (request.getVersion() == null || request.getVersion().isBlank())
                ? "1" : request.getVersion().strip();
        documentStore.findByExternalIdAndVersion(request.getExternalId(), version)
                .ifPresent(existing -> {
                    throw new ExtractionException("Document already exists with externalId '"
                            + request.getExternalId() + "' version '" + version
                            + "'. Use a new version label or the update endpoint.");
                });

        final String finalVersion = version;
        KbDocument document = documentStore.save(s -> {
            s.setExternalId(request.getExternalId());
            s.setName(request.getName());
            s.setSource(request.getSource());
            s.setCategory(request.getCategory());
            s.setTopic(request.getTopic());
            s.setVersion(finalVersion);
            s.setFormat(format);
            s.setContent(extracted);
            s.setPublicationDate(request.getPublicationDate());
            s.setUpdateDate(request.getUpdateDate());
            s.setActive(true);
        });

        // PDFs use the page-aware path so each chunk carries the originating page number.
        if (format == KbDocumentFormat.PDF && extractor instanceof PdfTextExtractor) {
            PdfTextExtractor pdfExtractor = (PdfTextExtractor) extractor;
            List<PdfTextExtractor.PageText> pages =
                    pdfExtractor.extractByPage(request.getPayload(), request.getExplicitText());
            chunkAndEmbedByPage(document, pages);
        } else {
            chunkAndEmbed(document, extracted);
        }
        log.info("Ingested document externalId={} version={} category={} format={}",
                document.getExternalId(), document.getVersion(), document.getCategory(), format);
        return document;
    }

    /**
     * Re-chunk and re-embed an already-stored document using its stored content,
     * without changing its metadata. Useful after the chunking or embedding
     * configuration changes.
     */
    @Transactional
    public KbDocument reindex(Long documentId) {
        KbDocument document = documentStore.findById(documentId)
                .orElseThrow(() -> new ExtractionException("Document not found: " + documentId));
        vectorStore.deleteByDocument(documentId);
        chunkRepository.deleteByDocumentId(documentId);
        chunkRepository.flush();
        chunkAndEmbed(document, document.getContent());
        log.info("Re-indexed document id={} chunks={}", documentId,
                chunkRepository.findByDocumentId(documentId).size());
        return document;
    }

    /**
     * Regenerate embeddings for all chunks of a document without re-chunking.
     */
    @Transactional
    public void regenerateEmbeddings(Long documentId) {
        KbDocument document = documentStore.findById(documentId)
                .orElseThrow(() -> new ExtractionException("Document not found: " + documentId));
        vectorStore.deleteByDocument(documentId);
        for (KbChunk chunk : chunkRepository.findByDocumentIdAndActiveTrue(documentId)) {
            vectorStore.store(chunk);
        }
        log.info("Regenerated embeddings for document id={}", documentId);
    }

    /**
     * Activate a particular version of a document and deactivate all other
     * versions of the same externalId. Returns the active document.
     */
    @Transactional
    public KbDocument activateVersion(String externalId, String version) {
        return documentStore.activateVersion(externalId, version, this::onActivate, this::onDeactivate);
    }

    /**
     * Deactivate a document (and therefore exclude its chunks from retrieval).
     */
    @Transactional
    public KbDocument deactivate(Long documentId) {
        KbDocument document = documentStore.findById(documentId)
                .orElseThrow(() -> new ExtractionException("Document not found: " + documentId));
        document.setActive(false);
        documentStore.save(document);
        onDeactivate(document);
        List<KbChunk> chunks = chunksOf(document);
        chunks.forEach(c -> c.setActive(false));
        chunkRepository.saveAll(chunks);
        return document;
    }

    private void onActivate(KbDocument doc) {
        List<KbChunk> chunks = chunksOf(doc);
        chunks.forEach(c -> c.setActive(true));
        chunkRepository.saveAll(chunks);
    }

    private void onDeactivate(KbDocument doc) {
        List<KbChunk> chunks = chunksOf(doc);
        chunks.forEach(c -> c.setActive(false));
        chunkRepository.saveAll(chunks);
    }

    private List<KbChunk> chunksOf(KbDocument doc) {
        return chunkRepository.findByDocumentId(doc.getId());
    }

    private void chunkAndEmbed(KbDocument document, String text) {
        List<Chunk> chunks = chunker.chunk(text);
        for (Chunk chunk : chunks) {
            persistChunk(document, chunk);
        }
    }

    private void chunkAndEmbedByPage(KbDocument document, List<PdfTextExtractor.PageText> pages) {
        List<Chunk> chunks = chunker.chunkByPage(pages);
        for (Chunk chunk : chunks) {
            persistChunk(document, chunk);
        }
    }

    private void persistChunk(KbDocument document, Chunk chunk) {
        KbChunk entity = new KbChunk();
        String chunkId = document.getExternalId() + "-v" + document.getVersion()
                + "-c" + chunk.sequence();
        entity.setChunkId(chunkId);
        entity.setDocument(document);
        entity.setChunkText(chunk.text());
        entity.setSequence(chunk.sequence());
        entity.setSection(chunk.section());
        entity.setPage(chunk.page());
        entity.setActive(true);
        KbChunk saved = chunkRepository.save(entity);
        try {
            vectorStore.store(saved);
        } catch (RuntimeException ex) {
            log.error("Embedding failed for chunk {}; chunk kept, search may be incomplete", chunkId, ex);
        }
    }

    private void validate(IngestDocumentRequest request) {
        if (request == null) {
            throw new ExtractionException("Ingest request cannot be null");
        }
        if (request.getExternalId() == null || request.getExternalId().isBlank()) {
            throw new ExtractionException("externalId is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ExtractionException("document name is required");
        }
        if (request.getCategory() == null) {
            throw new ExtractionException("category is required");
        }
if (request.getFormat() == null) {
            if (request.getExplicitText() != null && !request.getExplicitText().isBlank()) {
                request.setFormat(KbDocumentFormat.STRUCTURED);
            } else {
                throw new ExtractionException("format is required");
            }
        }
        // Validate that the resolved format is supported
        if (!List.of(
                KbDocumentFormat.PDF.name(),
                KbDocumentFormat.TXT.name(),
                KbDocumentFormat.MARKDOWN.name(),
                KbDocumentFormat.STRUCTURED.name()
        ).contains(request.getFormat().name())) {
            throw new ExtractionException("Unsupported document format: " + request.getFormat());
        }
        // Enforce payload size limits
        if (request.getPayload() != null && request.getPayload().length > 10_000_000) {
            throw new ExtractionException("Payload exceeds maximum allowed size of 10MB");
        }
        // Enforce explicit text size limits
        if (request.getExplicitText() != null && request.getExplicitText().length() > 5_000_000) {
            throw new ExtractionException("Explicit text exceeds maximum allowed length of 5MB");
        }
    }

    public List<String> supportedCategories() {
        return properties.supportedCategories();
    }

    public KbDocument findDocumentById(Long id) {
        return documentStore.findById(id)
                .orElseThrow(() -> new ExtractionException("Document not found: " + id));
    }

    public KbDocument findByExternalIdAndVersion(String externalId, String version) {
        return documentStore.findByExternalIdAndVersion(externalId, version)
                .orElseThrow(() -> new ExtractionException("Document not found: " + externalId + "/" + version));
    }

    public List<KbDocument> findAll() {
        return documentStore.findAll();
    }

    public List<KbDocument> findVersions(String externalId) {
        return documentStore.findVersions(externalId);
    }
}
