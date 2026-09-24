package com.healthcare.assistant.rag.retrieval;

import com.healthcare.assistant.rag.vector.VectorRecord;

import java.util.List;

/**
 * Immutable RAG context: the query, the assembled textual evidence block, the
 * citation list and the underlying hits. The grounding prompt, citation
 * validator and answer synthesiser all consume this object so the data only has
 * to be assembled once per query.
 */
public final class RagContext {

    private final String query;
    private final String contextBlock;
    private final List<Citation> citations;
    private final List<VectorRecord> hits;

    public RagContext(String query, String contextBlock, List<Citation> citations,
                      List<VectorRecord> hits) {
        this.query = query;
        this.contextBlock = contextBlock;
        this.citations = List.copyOf(citations);
        this.hits = List.copyOf(hits);
    }

    public static RagContext empty(String query) {
        return new RagContext(query, "", List.of(), List.of());
    }

    public String getQuery() {
        return query;
    }

    public String getContextBlock() {
        return contextBlock;
    }

    public List<Citation> getCitations() {
        return citations;
    }

    public List<VectorRecord> getHits() {
        return hits;
    }

    public boolean hasEvidence() {
        return !hits.isEmpty();
    }
}
