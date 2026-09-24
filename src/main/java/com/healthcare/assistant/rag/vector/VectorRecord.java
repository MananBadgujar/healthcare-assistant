package com.healthcare.assistant.rag.vector;

import com.healthcare.assistant.entity.KbChunk;

/**
 * Hit returned by the {@link VectorStore} during semantic search. Carries the
 * chunk metadata required to build the RAG context and generate citations
 * (document external id, name, category, topic, version, section, page and the
 * chunk text itself) plus the similarity score and, when {@code active=false},
 * indicates the chunk belongs to an inactive document version.
 *
 * @param chunkId      stable chunk identifier
 * @param text         chunk text
 * @param documentId   parent document id
 * @param externalId   parent document external id
 * @param documentName parent document name
 * @param category     parent document category name
 * @param topic        parent document topic
 * @param version      parent document version label
 * @param section      chunk section hint
 * @param page         chunk page (PDF) or null
 * @param similarity   cosine similarity score
 * @param active       whether the chunk is from an active document version
 */
public final class VectorRecord {

    private final String chunkId;
    private final String text;
    private final Long documentId;
    private final String externalId;
    private final String documentName;
    private final String category;
    private final String topic;
    private final String version;
    private final String section;
    private final Integer page;
    private final double similarity;
    private final boolean active;

    public VectorRecord(KbChunk chunk, String externalId, String documentName,
                        String category, String topic, String version,
                        double similarity, boolean active) {
        this.chunkId = chunk.getChunkId();
        this.text = chunk.getChunkText();
        this.documentId = chunk.getDocument().getId();
        this.externalId = externalId;
        this.documentName = documentName;
        this.category = category;
        this.topic = topic;
        this.version = version;
        this.section = chunk.getSection();
        this.page = chunk.getPage();
        this.similarity = similarity;
        this.active = active;
    }

    public String getChunkId() { return chunkId; }
    public String getText() { return text; }
    public Long getDocumentId() { return documentId; }
    public String getExternalId() { return externalId; }
    public String getDocumentName() { return documentName; }
    public String getCategory() { return category; }
    public String getTopic() { return topic; }
    public String getVersion() { return version; }
    public String getSection() { return section; }
    public Integer getPage() { return page; }
    public double getSimilarity() { return similarity; }
    public boolean isActive() { return active; }
}
