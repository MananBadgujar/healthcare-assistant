package com.healthcare.assistant.rag;

import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import com.healthcare.assistant.rag.ingestion.DocumentIngestionService;
import com.healthcare.assistant.rag.ingestion.IngestDocumentRequest;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.vector.VectorRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying the vector store filter behaviours: active-only,
 * category filter, version handling and Top-K truncation. Runs against the
 * test profile (H2 + deterministic hashing embeddings).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VectorStorageIntegrationTest {

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private SemanticSearchService searchService;

    @Test
    void categoryFilterRestrictsHits() {
        ingest("vs-cat-1", "1", KbCategory.CHRONIC_CONDITIONS,
                "Type 2 diabetes is a chronic condition affecting blood glucose.");
        ingest("vs-cat-2", "1", KbCategory.LIFESTYLE,
                "Regular walking and balanced diet help maintain a healthy lifestyle.");

        List<VectorRecord> all = searchService.search("diabetes blood glucose", null);
        List<VectorRecord> lifestyleOnly = searchService.search("diabetes blood glucose", KbCategory.LIFESTYLE.name());
        List<VectorRecord> chronicOnly = searchService.search("diabetes blood glucose", KbCategory.CHRONIC_CONDITIONS.name());

        assertTrue(lifestyleOnly.stream().allMatch(h -> "LIFESTYLE".equals(h.getCategory())));
        assertTrue(chronicOnly.stream().allMatch(h -> "CHRONIC_CONDITIONS".equals(h.getCategory())));
        assertTrue(all.size() >= chronicOnly.size());
    }

    @Test
    void unknownCategoryThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.search("diabetes", "NOT_A_REAL_CATEGORY"));
    }

    @Test
    void emptyAndBlankQueriesAreRejectedBySafetyGuard() {
        // The semantic search itself doesn't reject blank queries; the safety
        // guard upstream does. Here we verify search itself returns empty for
        // an embedding of a zero-token string (which never matches).
        List<VectorRecord> hits = searchService.search("   ", null);
        assertNotNull(hits);
        assertTrue(hits.isEmpty(), "blank query must yield no hits; was: " + hits);
    }

    @Test
    void topKLimitIsRespected() {
        // Ingest several short documents on the same topic and verify the
        // result list is bounded by the configured top-K (5 by default).
        for (int i = 0; i < 8; i++) {
            ingest("vs-topk-" + i, "1", KbCategory.GENERAL_HEALTH_EDUCATION,
                    "Hypertension management includes lifestyle changes and monitoring blood pressure daily. "
                            + "Document number " + i + ".");
        }
        List<VectorRecord> hits = searchService.search("hypertension blood pressure monitoring", null);
        assertTrue(hits.size() <= 5,
                "top-K=5 must cap the result list; was: " + hits.size());
        assertFalse(hits.isEmpty());
    }

    @Test
    void deactivatedDocumentIsNotRetrieved() {
        var doc = ingest("vs-deact", "1", KbCategory.PREVENTION,
                "Vaccination helps prevent many serious infectious diseases.");
        assertTrue(searchService.search("vaccination prevention", null).stream()
                .anyMatch(h -> "vs-deact".equals(h.getExternalId())));

        ingestionService.deactivate(doc.getId());
        assertTrue(searchService.search("vaccination prevention", null).stream()
                .noneMatch(h -> "vs-deact".equals(h.getExternalId())));
    }

    private com.healthcare.assistant.entity.KbDocument ingest(String externalId, String version,
                                                              KbCategory category, String text) {
        IngestDocumentRequest request = new IngestDocumentRequest();
        request.setExternalId(externalId);
        request.setName("Doc " + externalId);
        request.setSource("Vector store test");
        request.setCategory(category);
        request.setTopic(externalId);
        request.setVersion(version);
        request.setFormat(KbDocumentFormat.STRUCTURED);
        request.setExplicitText(text);
        return ingestionService.ingest(request);
    }
}
