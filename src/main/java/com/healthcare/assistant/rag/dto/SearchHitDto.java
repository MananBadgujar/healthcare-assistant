package com.healthcare.assistant.rag.dto;

import com.healthcare.assistant.rag.vector.VectorRecord;

/**
 * Search hit returned by {@code GET /api/v1/kb/search}. Excludes the raw chunk
 * text length but includes a snippet for citation purposes; the full chunk text
 * is returned only via the harder-coupled conversational flow.
 */
public class SearchHitDto {

    private final String document;
    private final String documentExternalId;
    private final String category;
    private final String topic;
    private final String version;
    private final String section;
    private final Integer page;
    private final double similarity;
    private final String snippet;

    public SearchHitDto(VectorRecord record, int snippetLen) {
        this.document = record.getDocumentName();
        this.documentExternalId = record.getExternalId();
        this.category = record.getCategory();
        this.topic = record.getTopic();
        this.version = record.getVersion();
        this.section = record.getSection();
        this.page = record.getPage();
        this.similarity = record.getSimilarity();
        String text = record.getText() == null ? "" : record.getText();
        this.snippet = text.length() <= snippetLen ? text : text.substring(0, snippetLen) + "...";
    }

    public String getDocument() { return document; }
    public String getDocumentExternalId() { return documentExternalId; }
    public String getCategory() { return category; }
    public String getTopic() { return topic; }
    public String getVersion() { return version; }
    public String getSection() { return section; }
    public Integer getPage() { return page; }
    public double getSimilarity() { return similarity; }
    public String getSnippet() { return snippet; }
}
