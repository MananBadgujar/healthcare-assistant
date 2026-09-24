package com.healthcare.assistant.rag.generation;

import com.healthcare.assistant.rag.config.RagProperties;
import com.healthcare.assistant.rag.retrieval.RagContext;
import com.healthcare.assistant.service.AiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Calls the LLM through the existing {@link AiProvider} abstraction (backed by
 * {@code OllamaChatService}) with the grounded prompt. When the LLM is
 * unavailable or returns an unsafe placeholder, the synthesiser falls back to
 * the deterministic insufficient-evidence message so the pipeline never blocks
 * or leaks raw error text to the patient.
 */
@Service
public class RagAnswerSynthesiser {

    private static final Logger log = LoggerFactory.getLogger(RagAnswerSynthesiser.class);

    private final AiProvider aiProvider;
    private final RagProperties properties;

    @Autowired
    public RagAnswerSynthesiser(AiProvider aiProvider, RagProperties properties) {
        this.aiProvider = aiProvider;
        this.properties = properties;
    }

    /**
     * Generate an answer for the given context.
     *
     * @param context              retrieved RAG context
     * @param patientContextSummary optional patient context summary
     * @param conversationHistory  bounded prior turns
     * @return raw LLM answer, or the deterministic fallback when unavailable
     */
    public String synthesise(RagContext context, String patientContextSummary,
                             List<String> conversationHistory) {
        if (!context.hasEvidence()) {
            return GroundedPromptBuilder.insufficientEvidenceAnswer();
        }
        String prompt = GroundedPromptBuilder.build(context, patientContextSummary, conversationHistory);
        try {
            String response = aiProvider.generateResponse(prompt);
            if (response == null || response.isBlank()) {
                log.warn("LLM returned empty answer; using fallback");
                return GroundedPromptBuilder.insufficientEvidenceAnswer();
            }
            // Catch OllamaChatService safety fallback text and replace with the
            // RAG-specific safe message so the validator can mark it ungrounded.
            String trimmed = response.strip();
            if (trimmed.startsWith("Safety override:")) {
                log.warn("LLM returned safety override fallback; propagating as insufficient evidence");
                return GroundedPromptBuilder.insufficientEvidenceAnswer();
            }
            return trimmed;
        } catch (RuntimeException ex) {
            log.error("LLM call failed: {}", ex.getMessage());
            if (properties.getGeneration().isFallbackOnLlmError()) {
                return GroundedPromptBuilder.insufficientEvidenceAnswer();
            }
            throw ex;
        }
    }
}
