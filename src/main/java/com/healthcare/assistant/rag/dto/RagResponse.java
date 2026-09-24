package com.healthcare.assistant.rag.dto;

import com.healthcare.assistant.rag.retrieval.Citation;

import java.util.List;

/**
 * Structured patient-education RAG response.
 * <p>
 * Returned by the public {@code /api/v1/kb/ask} endpoint and the conversational
 * education flow. The structure matches the required Phase 9 contract:
 *
 * <pre>
 * {
 *   "answer": "...",
 *   "sources": [ { "document": "...", "section": "...", "page": "...", "version": "..." } ],
 *   "confidence": "HIGH|MODERATE|LOW|INSUFFICIENT",
 *   "grounded": true,
 *   "requiresProviderReview": false,
 *   "disclaimer": "..."
 * }
 * </pre>
 */
public class RagResponse {

    private String answer;
    private List<Citation> sources;
    /** HIGH / MODERATE / LOW / INSUFFICIENT. */
    private String confidence;
    private boolean grounded;
    private boolean requiresProviderReview;
    private String disclaimer;
    /** Stable conversation id associated with this exchange (set when the ask endpoint is used in conversation mode). */
    private String conversationId;

    public RagResponse() {
    }

    public static RagResponse ungroundedFallback(String answer, String disclaimer) {
        RagResponse response = new RagResponse();
        response.answer = answer;
        response.sources = List.of();
        response.confidence = "INSUFFICIENT";
        response.grounded = false;
        response.requiresProviderReview = true;
        response.disclaimer = disclaimer;
        return response;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<Citation> getSources() { return sources; }
    public void setSources(List<Citation> sources) { this.sources = sources; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }

    public boolean isRequiresProviderReview() { return requiresProviderReview; }
    public void setRequiresProviderReview(boolean requiresProviderReview) {
        this.requiresProviderReview = requiresProviderReview;
    }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
