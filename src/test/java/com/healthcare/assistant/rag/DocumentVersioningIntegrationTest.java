package com.healthcare.assistant.rag;

import com.healthcare.assistant.entity.KbChunk;
import com.healthcare.assistant.entity.KbDocument;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import com.healthcare.assistant.rag.ingestion.DocumentIngestionService;
import com.healthcare.assistant.rag.ingestion.IngestDocumentRequest;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.vector.VectorRecord;
import com.healthcare.assistant.repository.KbChunkRepository;
import com.healthcare.assistant.repository.KbEmbeddingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test exercising the full document-versioning lifecycle:
 * version 1 ingestion, version 2 ingestion, activation of v2, verification
 * that v1 is deactivated, search returns only v2 chunks, and that re-index /
 * regenerate-embeddings preserve chunk-level metadata.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentVersioningIntegrationTest {

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private SemanticSearchService searchService;

    @Autowired
    private KbChunkRepository chunkRepository;

    @Autowired
    private KbEmbeddingRepository embeddingRepository;

    @Test
    void versioningLifecycleActivatesLatestAndDeactivatesPrevious() {
        KbDocument v1 = ingestVersion("v1", "Initial hypertension management guidance for patients with elevated blood pressure.");
        KbDocument v2 = ingestVersion("v2", "Updated hypertension management guidance includes lifestyle changes and medication review.");

        // Initially v1 is active and v2 is active too (every newly ingested version is active=true).
        assertTrue(v1.isActive());
        assertTrue(v2.isActive());

        // Activate v2: v1 must become inactive.
        KbDocument active = ingestionService.activateVersion("versioned-doc", "v2");
        assertTrue(active.isActive());
        assertEquals("v2", active.getVersion());

        KbDocument reloadedV1 = ingestionService.findByExternalIdAndVersion("versioned-doc", "v1");
        assertFalse(reloadedV1.isActive(), "activating v2 must deactivate v1");

        // Sanity: confirm v2 chunks exist and are marked active.
        List<KbChunk> v2Chunks = chunkRepository.findByDocumentId(active.getId());
        assertFalse(v2Chunks.isEmpty(), "v2 must have chunks");
        assertTrue(v2Chunks.stream().allMatch(KbChunk::isActive),
                "v2 chunks must be active after activation: " + v2Chunks);

        // Retrieval must only return v2 chunks. Use a query whose tokens are
        // present in v2's content.
        List<VectorRecord> hits = searchService.search("hypertension management lifestyle changes", null);
        boolean sawV1 = hits.stream().anyMatch(h -> "v1".equals(h.getVersion()));
        boolean sawV2 = hits.stream().anyMatch(h -> "v2".equals(h.getVersion()));
        assertFalse(sawV1, "v1 chunks must not appear in retrieval: " + hits);
        assertTrue(sawV2, "v2 chunks must appear in retrieval: " + hits);

        // Citations must carry the active version.
        for (VectorRecord hit : hits) {
            if ("versioned-doc".equals(hit.getExternalId())) {
                assertEquals("v2", hit.getVersion());
            }
        }
    }

    @Test
    void reindexPreservesChunksAndRefreshesEmbeddings() {
        KbDocument v1 = ingestVersion("v1", "Asthma is a chronic respiratory condition affecting the airways and breathing.");
        long initialChunkCount = chunkRepository.findByDocumentId(v1.getId()).size();
        long initialEmbeddingCount = embeddingRepository.findByChunk_DocumentId(v1.getId()).size();
        assertTrue(initialChunkCount > 0, "ingestion must produce chunks");
        assertEquals(initialChunkCount, initialEmbeddingCount, "every chunk must have an embedding");

        KbDocument reindexed = ingestionService.reindex(v1.getId());
        assertNotNull(reindexed);
        long afterChunkCount = chunkRepository.findByDocumentId(v1.getId()).size();
        long afterEmbeddingCount = embeddingRepository.findByChunk_DocumentId(v1.getId()).size();
        assertEquals(initialChunkCount, afterChunkCount, "reindex must preserve chunk count");
        assertEquals(initialChunkCount, afterEmbeddingCount, "reindex must refresh all embeddings");
    }

    @Test
    void deactivateRemovesDocumentFromRetrieval() {
        KbDocument v1 = ingestVersion("v1", "Migraine is a neurological condition causing severe recurring headaches.");
        // Confirm retrieval finds it.
        assertTrue(searchService.search("migraine headache", null).stream()
                .anyMatch(h -> "versioned-doc".equals(h.getExternalId())));

        KbDocument deactivated = ingestionService.deactivate(v1.getId());
        assertFalse(deactivated.isActive());

        List<VectorRecord> hits = searchService.search("migraine headache", null);
        boolean saw = hits.stream().anyMatch(h -> "versioned-doc".equals(h.getExternalId()));
        assertFalse(saw, "deactivated document must not be returned by retrieval: " + hits);

        // Chunks must also be marked inactive.
        for (KbChunk chunk : chunkRepository.findByDocumentId(v1.getId())) {
            assertFalse(chunk.isActive(), "deactivation must mark every chunk inactive");
        }
    }

    @Test
    void versionMetadataIsReturnedInSearchHitsAndCitations() {
        KbDocument v1 = ingestVersion("v1", "Diabetes management includes regular blood glucose monitoring and dietary changes.");
        List<VectorRecord> hits = searchService.search("diabetes blood glucose", null);
        boolean saw = false;
        for (VectorRecord hit : hits) {
            if ("versioned-doc".equals(hit.getExternalId())) {
                saw = true;
                assertEquals("v1", hit.getVersion(), "citation version must match the document version");
                assertNotNull(hit.getDocumentName());
                assertNotNull(hit.getCategory());
                assertNotNull(hit.getExternalId());
            }
        }
        assertTrue(saw, "search must surface the ingested document");
    }

    private KbDocument ingestVersion(String version, String content) {
        IngestDocumentRequest request = new IngestDocumentRequest();
        request.setExternalId("versioned-doc");
        request.setName("Versioned Doc " + version);
        request.setSource("Test source");
        request.setCategory(KbCategory.GENERAL_HEALTH_EDUCATION);
        request.setTopic("Versioning");
        request.setVersion(version);
        request.setFormat(KbDocumentFormat.STRUCTURED);
        request.setExplicitText(content);
        return ingestionService.ingest(request);
    }
}
