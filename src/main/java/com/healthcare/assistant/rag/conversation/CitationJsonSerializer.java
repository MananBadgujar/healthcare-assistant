package com.healthcare.assistant.rag.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.rag.dto.RagResponse;
import com.healthcare.assistant.rag.retrieval.Citation;

import java.util.List;

/**
 * Serialises a list of {@link Citation} objects (or the sources of a
 * {@link RagResponse}) to a compact JSON string for storage on the
 * {@link com.healthcare.assistant.entity.EducationMessage} entity.
 * <p>
 * Centralising this keeps the conversation service free of JSON concerns and
 * means the persisted shape is consistent for the citation validation replay
 * and the feedback workflow.
 */
public final class CitationJsonSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CitationJsonSerializer() {
    }

    public static String serialise(List<Citation> citations) {
        if (citations == null || citations.isEmpty()) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(citations);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    public static String serialise(RagResponse response) {
        if (response == null || response.getSources() == null) {
            return "[]";
        }
        return serialise(response.getSources());
    }
}
