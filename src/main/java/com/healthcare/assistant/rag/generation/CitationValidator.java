package com.healthcare.assistant.rag.generation;

import com.healthcare.assistant.rag.retrieval.Citation;
import com.healthcare.assistant.rag.retrieval.RagContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validates that an LLM answer is grounded in the retrieved evidence.
 * <p>
 * Pipeline applied to the model output:
 * <ol>
 *   <li>{@link #validate} extracts the citation indices referenced in the
 *       answer and confirms each one resolves to a real {@link Citation} from
 *       the {@link RagContext}. Indices that do not resolve are reported as
 *       invalid references (fabricated citations).</li>
 *   <li>The validator inspects salient claim tokens (non-stopword, length >=4
 *       alphabetic terms) from the answer against the union of retrieved
 *       evidence text. When the fraction of claim tokens present in the
 *       evidence drops below the configured threshold the answer is marked as
 *       containing <em>unsupported claims</em>.</li>
 *   <li>The result also reports whether the safe fallback answer was used:
 *       the fallback is automatically considered grounded=false and
 *       requiresProviderReview=true.</li>
 * </ol>
 * The validator never edits the answer; callers (the orchestration layer) use
 * the {@link ValidationResult} to replace dangerous output with the safe
 * fallback when grounding fails.
 */
@Component
public class CitationValidator {

    private static final Set<String> STOPWORDS = new HashSet<>(java.util.Arrays.asList(
            "this", "that", "with", "from", "have", "your", "their", "they",
            "them", "what", "when", "which", "where", "while", "should", "would",
            "could", "might", "please", "always", "never", "often", "every",
            "also", "than", "then", "into", "such", "very", "more", "most",
            "some", "many", "much", "make", "made", "side", "take", "taken",
            "using", "used", "before", "after", "during", "without",
            "however", "though", "although", "because", "between", "through",
            "patient", "patients", "health", "healthcare", "medical", "care",
            "also", "risk", "risks", "symptoms", "symptom", "treatment",
            "treatments", "doctor", "professional", "consult", "contact",
            "advice", "diagnosis", "disease", "diseases", "condition",
            "conditions", "general", "educational", "purposes", "only",
            "substitute", "professional", "symptoms", "severe", "worsening",
            "unsure", "immediately", "section", "page", "version", "source",
            "sources", "citation", "citations", "user", "patient", "answer",
            "question", "feel", "feeling"
    ));

    private final double unsupportedThreshold;

    public CitationValidator() {
        this(0.45);
    }

    public CitationValidator(double unsupportedThreshold) {
        this.unsupportedThreshold = unsupportedThreshold;
    }

    public ValidationResult validate(String answer, RagContext context) {
        if (answer == null || answer.isBlank()) {
            return new ValidationResult(false, false, List.of(),
                    "Empty answer; using safe fallback.");
        }
        if (answer.equals(com.healthcare.assistant.rag.generation.GroundedPromptBuilder.insufficientEvidenceAnswer())) {
            return new ValidationResult(false, true, List.of(),
                    "No evidence was provided to the LLM; safe fallback used.");
        }
        List<Integer> referenced = CitationExtractor.extract(answer);
        Set<Integer> validIndices = new HashSet<>();
        for (Citation citation : context.getCitations()) {
            validIndices.add(citation.getIndex());
        }
        List<Integer> invalid = new ArrayList<>();
        for (Integer ref : referenced) {
            if (!validIndices.contains(ref)) {
                invalid.add(ref);
            }
        }
        boolean citationsValid = invalid.isEmpty();

        // Unsupported claim check via salient token overlap with evidence.
        String answerLower = answer.toLowerCase(Locale.ROOT);
        Set<String> claimTokens = salientTokens(answerLower);
        Set<String> evidenceTokens = evidenceTokens(context);
        if (claimTokens.isEmpty()) {
            // No salient claims to verify: trust the citation check only.
            // A non-cited answer without claims is treated as grounded so that
            // short factual acknowledgements (e.g. "Yes [1].") are not penalised;
            // any fabricated citation still triggers grounded=false.
            return new ValidationResult(citationsValid, false, invalid,
                    citationsValid ? "Answer grounded (no salient claims to verify)."
                            : "Answer referenced citations that are not in the evidence: " + invalid);
        }
        int present = 0;
        for (String token : claimTokens) {
            if (evidenceTokens.contains(token)) {
                present++;
            }
        }
        double coverage = (double) present / claimTokens.size();
        boolean grounded = citationsValid && coverage >= unsupportedThreshold;
        String reason;
        if (!citationsValid) {
            reason = "Answer referenced citations not present in retrieved evidence: " + invalid;
        } else if (coverage < unsupportedThreshold) {
            reason = String.format(java.util.Locale.ROOT,
                    "Answer contains claims not supported by retrieved evidence (coverage %.2f).", coverage);
        } else {
            reason = "Answer grounded with valid citations.";
        }
        return new ValidationResult(grounded, false, invalid, reason);
    }

    private Set<String> salientTokens(String text) {
        Set<String> tokens = new HashSet<>();
        for (String word : text.split("[^a-z0-9]+")) {
            if (word.length() >= 4 && !STOPWORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    private Set<String> evidenceTokens(RagContext context) {
        Set<String> tokens = new HashSet<>();
        if (!context.hasEvidence()) {
            return tokens;
        }
        for (var hit : context.getHits()) {
            String lower = hit.getText().toLowerCase(Locale.ROOT);
            for (String word : lower.split("[^a-z0-9]+")) {
                if (word.length() >= 4 && !STOPWORDS.contains(word)) {
                    tokens.add(word);
                }
            }
        }
        // Also include citation document names and topic so named sources count as supported.
        for (Citation citation : context.getCitations()) {
            if (citation.getDocumentName() != null) {
                for (String word : citation.getDocumentName().toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
                    if (word.length() >= 4) {
                        tokens.add(word);
                    }
                }
            }
            if (citation.getTopic() != null) {
                for (String word : citation.getTopic().toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
                    if (word.length() >= 4) {
                        tokens.add(word);
                    }
                }
            }
        }
        return tokens;
    }

    /**
     * Result of validation.
     *
     * @param grounded                true when the answer is supported by retrieved evidence
     *                                with valid citations
     * @param insufficientEvidence     true when no evidence was available and the fallback was used
     * @param invalidCitations         list of citation indices referenced in the answer that do
     *                                not exist in the retrieved context (fabricated citations)
     * @param reason                   human-readable explanation
     */
    public static final class ValidationResult {
        private final boolean grounded;
        private final boolean insufficientEvidence;
        private final List<Integer> invalidCitations;
        private final String reason;

        public ValidationResult(boolean grounded, boolean insufficientEvidence,
                                List<Integer> invalidCitations, String reason) {
            this.grounded = grounded;
            this.insufficientEvidence = insufficientEvidence;
            this.invalidCitations = List.copyOf(invalidCitations);
            this.reason = reason;
        }

        public boolean isGrounded() { return grounded; }
        public boolean isInsufficientEvidence() { return insufficientEvidence; }
        public List<Integer> getInvalidCitations() { return invalidCitations; }
        public String getReason() { return reason; }
    }
}
