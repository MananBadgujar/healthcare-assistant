package com.healthcare.assistant.rag.conversation;

/**
 * Payload describing the assistant answer to persist into an education
 * conversation message. The fields mirror the structured
 * {@link com.healthcare.assistant.rag.dto.RagResponse}; the conversation
 * service uses this small value object to avoid coupling the persistence layer
 * to the API DTO.
 */
public final class RagAssistantPayload {

    private final String answer;
    private final String citationsJson;
    private final boolean grounded;
    private final boolean requiresProviderReview;
    private final String confidence;

    public RagAssistantPayload(String answer, String citationsJson, boolean grounded,
                               boolean requiresProviderReview, String confidence) {
        this.answer = answer;
        this.citationsJson = citationsJson;
        this.grounded = grounded;
        this.requiresProviderReview = requiresProviderReview;
        this.confidence = confidence;
    }

    public String getAnswer() { return answer; }
    public String getCitationsJson() { return citationsJson; }
    public boolean isGrounded() { return grounded; }
    public boolean isRequiresProviderReview() { return requiresProviderReview; }
    public String getConfidence() { return confidence; }
}
