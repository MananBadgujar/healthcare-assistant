package com.healthcare.assistant.rag.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/kb/ask}.
 *
 * <p>The {@code question} is the only required field. Optional fields:
 * <ul>
 *   <li>{@code conversationId} — when set, the request continues an existing
 *       conversational session; when absent, a new session is created and its
 *       id is returned in the response.</li>
 *   <li>{@code category} — optional category filter restricting retrieval to a
 *       single knowledge-base category (e.g. "MEDICATIONS").</li>
 *   <li>{@code includePatientContext} — when false (default true for patients)
 *       the patient context summary is omitted from the LLM prompt.</li>
 * </ul>
 */
public class AskRequest {

    @NotBlank(message = "question is required")
    private String question;

    private String conversationId;

    private String category;

    private boolean includePatientContext = true;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isIncludePatientContext() {
        return includePatientContext;
    }

    public void setIncludePatientContext(boolean includePatientContext) {
        this.includePatientContext = includePatientContext;
    }
}
