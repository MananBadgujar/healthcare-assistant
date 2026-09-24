package com.healthcare.assistant.rag.vector;

import com.healthcare.assistant.entity.KbChunk;
import com.healthcare.assistant.entity.KbEmbedding;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.rag.config.RagProperties;
import com.healthcare.assistant.rag.embedding.EmbeddingService;
import com.healthcare.assistant.repository.KbEmbeddingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vector storage and search façade.
 * <p>
 * Embeddings are persisted through {@link KbEmbeddingRepository} and retrieved
 * by scanning persisted vectors, computing cosine similarity against the query
 * embedding and ranking. This intentionally avoids introducing a separate vector
 * database; the same Spring Data JPA stack that backs the rest of the
 * application is reused, which keeps the test profile portable across MySQL and
 * H2. Retrieval applies the configured Top-K, similarity threshold, active-only
 * filter and optional category filter.
 */
@Service
public class VectorStore {

    private final KbEmbeddingRepository embeddingRepository;
    private final EmbeddingService embeddingService;
    private final RagProperties properties;

    public VectorStore(KbEmbeddingRepository embeddingRepository,
                       EmbeddingService embeddingService,
                       RagProperties properties) {
        this.embeddingRepository = embeddingRepository;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }

    /** Generate and persist an embedding for the supplied chunk. */
    public KbEmbedding store(KbChunk chunk) {
        embeddingRepository.findByChunkId(chunk.getId()).ifPresent(embeddingRepository::delete);
        float[] vector = embeddingService.embed(chunk.getChunkText());
        KbEmbedding embedding = new KbEmbedding();
        embedding.setChunk(chunk);
        embedding.setEmbeddingModel(embeddingService.providerName());
        embedding.setDimension(vector.length);
        embedding.setVector(VectorMath.encode(vector));
        return embeddingRepository.save(embedding);
    }

    /** Remove embeddings for a chunk (used when chunks are dropped). */
    public void deleteByChunk(KbChunk chunk) {
        embeddingRepository.findByChunkId(chunk.getId()).ifPresent(embeddingRepository::delete);
    }

    /** Remove all embeddings for a document (used when re-indexing). */
    public void deleteByDocument(Long documentId) {
        embeddingRepository.findByChunk_DocumentId(documentId)
                .forEach(embeddingRepository::delete);
    }

    /**
     * Semantic search the vector store.
     *
     * @param query    user question
     * @param category optional category filter, null/empty for none
     * @return ranked list of {@link VectorRecord} capped at Top-K
     */
    public List<VectorRecord> search(String query, KbCategory category) {
        float[] queryVector = embeddingService.embed(query);
        if (queryVector.length == 0) {
            return List.of();
        }
        RagProperties.Retrieval cfg = properties.getRetrieval();
        List<Scored> scored = new ArrayList<>();
        for (KbEmbedding embedding : embeddingRepository.findAll()) {
            KbChunk chunk = embedding.getChunk();
            if (chunk == null || chunk.getDocument() == null) {
                continue;
            }
            boolean active = chunk.isActive() && chunk.getDocument().isActive();
            if (cfg.isActiveOnly() && !active) {
                continue;
            }
            if (category != null && chunk.getDocument().getCategory() != category) {
                continue;
            }
            float[] vector = VectorMath.decode(embedding.getVector());
            if (vector.length != queryVector.length) {
                // Skip incompatible vectors (e.g. embeddings generated with a
                // different dimension); they will be re-embedded on the next
                // re-index operation.
                continue;
            }
            double similarity = VectorMath.cosineSimilarity(queryVector, vector);
            if (similarity < cfg.getSimilarityThreshold()) {
                continue;
            }
            scored.add(new Scored(chunk,
                    chunk.getDocument().getExternalId(),
                    chunk.getDocument().getName(),
                    chunk.getDocument().getCategory().name(),
                    chunk.getDocument().getTopic(),
                    chunk.getDocument().getVersion(),
                    similarity,
                    active));
        }
        scored.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        int topK = Math.max(1, cfg.getTopK());
        return scored.stream()
                .limit(topK)
                .map(s -> new VectorRecord(s.chunk, s.externalId, s.documentName,
                        s.category, s.topic, s.version, s.similarity, s.active))
                .collect(Collectors.toList());
    }

    private static final class Scored {
        final KbChunk chunk;
        final String externalId;
        final String documentName;
        final String category;
        final String topic;
        final String version;
        final double similarity;
        final boolean active;

        Scored(KbChunk chunk, String externalId, String documentName, String category,
               String topic, String version, double similarity, boolean active) {
            this.chunk = chunk;
            this.externalId = externalId;
            this.documentName = documentName;
            this.category = category;
            this.topic = topic;
            this.version = version;
            this.similarity = similarity;
            this.active = active;
        }
    }
}
