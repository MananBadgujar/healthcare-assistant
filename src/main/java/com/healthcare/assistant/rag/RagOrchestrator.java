package com.healthcare.assistant.rag;

import com.healthcare.assistant.rag.dto.RagResponse;
import com.healthcare.assistant.rag.generation.CitationValidator;
import com.healthcare.assistant.rag.generation.GroundedPromptBuilder;
import com.healthcare.assistant.rag.generation.RagAnswerSynthesiser;
import com.healthcare.assistant.rag.retrieval.RagContext;
import com.healthcare.assistant.rag.retrieval.RagContextBuilder;
import com.healthcare.assistant.rag.retrieval.RetrievedEvidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the end-to-end RAG pipeline: retrieval -> context building ->
 * LLM synthesis -> citation validation -> structured response.
 * <p>
 * The orchestrator is the single entrypoint used by both the conversational and
 * the one-shot ask flows. It owns the deterministic safety contract: when no
 * evidence is retrieved or when validation reports unsupported claims, the
 * answer is replaced with the safe fallback and the structured response is
 * flagged {@code grounded=false} and {@code requiresProviderReview=true}.
 */
@Service
public class RagOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(RagOrchestrator.class);

    private final RagContextBuilder contextBuilder;
    private final RagAnswerSynthesiser synthesiser;
    private final CitationValidator citationValidator;

    public RagOrchestrator(RagContextBuilder contextBuilder,
                           RagAnswerSynthesiser synthesiser,
                           CitationValidator citationValidator) {
        this.contextBuilder = contextBuilder;
        this.synthesiser = synthesiser;
        this.citationValidator = citationValidator;
    }

    /**
     * Build the final structured response for a question.
     *
     * @param evidence               retrieved evidence for the question
     * @param patientContextSummary  optional authorised patient context summary
     * @param conversationHistory    bounded prior conversation turns
     * @return structured {@link RagResponse} (never null)
     */
    public RagResponse answer(RetrievedEvidence evidence, String patientContextSummary,
                              List<String> conversationHistory) {
        RagContext context = contextBuilder.build(evidence);
        if (!context.hasEvidence()) {
            log.info("No evidence retrieved for query; returning safe fallback");
            return RagResponse.ungroundedFallback(
                    GroundedPromptBuilder.insufficientEvidenceAnswer(),
                    GroundedPromptBuilder.DISCLAIMER);
        }

        String rawAnswer = synthesiser.synthesise(context, patientContextSummary, conversationHistory);
        CitationValidator.ValidationResult validation = citationValidator.validate(rawAnswer, context);

        RagResponse response = new RagResponse();
        response.setSources(context.getCitations());
        response.setDisclaimer(GroundedPromptBuilder.DISCLAIMER);
        response.setConversationId(null);

        if (validation.isInsufficientEvidence()) {
            // Fallback was synthesised.
            response.setAnswer(GroundedPromptBuilder.insufficientEvidenceAnswer());
            response.setGrounded(false);
            response.setRequiresProviderReview(true);
            response.setConfidence("INSUFFICIENT");
            log.info("LLM used insufficient-evidence fallback");
            return response;
        }
        if (!validation.isGrounded()) {
            log.warn("Answer failed citation validation: {}", validation.getReason());
            response.setAnswer(GroundedPromptBuilder.insufficientEvidenceAnswer());
            response.setGrounded(false);
            response.setRequiresProviderReview(true);
            response.setConfidence("LOW");
            return response;
        }
        response.setAnswer(sanitiseAnswer(rawAnswer));
        response.setGrounded(true);
        response.setRequiresProviderReview(false);
        response.setConfidence(confidenceFor(validation, context));
        return response;
    }

    private String sanitiseAnswer(String answer) {
        // Remove any stray "Safety override:" leak just in case the model echoed our text.
        String clean = answer.replaceAll("(?i)Safety override:.*", "").strip();
        // Ensure the disclaimer is not duplicated from the model output; the
        // structured response already carries it separately.
        return clean;
    }

    private String confidenceFor(CitationValidator.ValidationResult validation, RagContext context) {
        double bestSimilarity = context.getHits().stream()
                .mapToDouble(h -> h.getSimilarity())
                .max()
                .orElse(0.0);
        if (bestSimilarity >= 0.65) {
            return "HIGH";
        }
        if (bestSimilarity >= 0.45) {
            return "MODERATE";
        }
        return "LOW";
    }
}
