package com.healthcare.assistant.rag.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the public semantic-search endpoint.
 */
public class SearchRequest {

    @NotBlank
    private String query;

    private String category;
    private int topK = 5;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
}
