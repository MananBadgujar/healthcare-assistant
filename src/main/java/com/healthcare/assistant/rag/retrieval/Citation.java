package com.healthcare.assistant.rag.retrieval;

/**
 * A citation returned with a RAG answer. Citations only reference documents and
 * chunks that were actually retrieved from the knowledge base; the LLM is never
 * allowed to invent a citation.
 *
 * @param index        1-based citation index used in the LLM prompt
 * @param documentName human-friendly document name
 * @param externalId   stable external document identifier
 * @param section      section hint when known
 * @param page         page number when known (PDF), otherwise null
 * @param version      document version label
 * @param topic        document topic
 * @param category     document category name
 * @param similarity    retrieval similarity score
 */
public final class Citation {

    private final int index;
    private final String externalId;
    private final String documentName;
    private final String section;
    private final Integer page;
    private final String version;
    private final String topic;
    private final String category;
    private final double similarity;

    public Citation(int index, String externalId, String documentName, String section,
                    Integer page, String version, String topic, String category,
                    double similarity) {
        this.index = index;
        this.externalId = externalId;
        this.documentName = documentName;
        this.section = section;
        this.page = page;
        this.version = version;
        this.topic = topic;
        this.category = category;
        this.similarity = similarity;
    }

    public int getIndex() { return index; }
    public String getExternalId() { return externalId; }
    public String getDocumentName() { return documentName; }
    public String getSection() { return section; }
    public Integer getPage() { return page; }
    public String getVersion() { return version; }
    public String getTopic() { return topic; }
    public String getCategory() { return category; }
    public double getSimilarity() { return similarity; }
}
