package com.healthcare.assistant.rag.retrieval;

import com.healthcare.assistant.rag.vector.VectorRecord;

import java.util.List;

/**
 * Result of running RAG retrieval for a user question: the ranked list of
 * retrieved evidence (chunks together with their traceable source metadata) and
 * a flag indicating whether any evidence was found above the relevance
 * threshold. {@code RetrievedEvidence} is the value object the context builder,
 * citation generator and citation validator operate on, so they never need to
 * touch the database directly.
 */
public final class RetrievedEvidence {

    private final String query;
    private final List<VectorRecord> hits;
    private final boolean hasEvidence;

    public RetrievedEvidence(String query, List<VectorRecord> hits) {
        this.query = query;
        this.hits = hits == null ? List.of() : List.copyOf(hits);
        this.hasEvidence = !this.hits.isEmpty();
    }

    public String getQuery() {
        return query;
    }

    public List<VectorRecord> getHits() {
        return hits;
    }

    public boolean hasEvidence() {
        return hasEvidence;
    }
}
