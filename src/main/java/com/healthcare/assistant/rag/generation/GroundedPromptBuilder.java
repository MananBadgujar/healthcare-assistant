package com.healthcare.assistant.rag.generation;

import com.healthcare.assistant.rag.retrieval.RagContext;

/**
 * Builds the grounded instruction prompt sent to the LLM.
 * <p>
 * The prompt explicitly instructs the model to:
 * <ul>
 *   <li>use only the supplied knowledge-base evidence for factual healthcare
 *       claims;</li>
 *   <li>not invent sources or fabricate citations;</li>
 *   <li>not claim certainty when evidence is insufficient;</li>
 *   <li>not provide unsupported diagnosis or invent patient-specific facts;</li>
 *   <li>clearly distinguish educational information from personalised medical
 *       advice;</li>
 *   <li>reference evidence using the deterministic citation indices assigned by
 *       the context builder;</li>
 *   <li>praise caution: include warning signs and when to seek professional
 *       help.</li>
 * </ul>
 * The prompt is generated deterministically from the {@link RagContext}, so the
 * citation validator can re-check the model's references against the supplied
 * indices afterwards.
 */
public final class GroundedPromptBuilder {

    /** Standard disclaimer appended to every answer. */
    public static final String DISCLAIMER =
            "This information is for general educational purposes only and is not a personal medical " +
            "diagnosis or a substitute for professional healthcare advice. If symptoms are severe, " +
            "worsening, or you are unsure, contact a qualified healthcare provider immediately.";

    /** Safe fallback returned when there is no grounded evidence. */
    public static final String INSUFFICIENT_EVIDENCE_FALLBACK =
            "I don't have enough information in the available knowledge base to answer this reliably. " +
            "Please consult a qualified healthcare professional for guidance specific to your situation.";

    private GroundedPromptBuilder() {
    }

    public static String build(RagContext context) {
        return build(context, null, java.util.Collections.emptyList());
    }

    /**
     * Build the grounded prompt.
     *
     * @param context              retrieved RAG context
     * @param patientContextSummary optional short summary of authorised patient
     *                              context (null/blank when none)
     * @param conversationHistory  bounded prior conversation turns (user +
     *                              assistant), already rendered as plain text
     */
    public static String build(RagContext context, String patientContextSummary,
                               java.util.List<String> conversationHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a careful, trustworthy patient-education assistant for a healthcare application.\n");
        prompt.append("Answer the patient's question using ONLY the supplied knowledge-base evidence.\n\n");

        prompt.append("STRICT RULES\n");
        prompt.append("- Use only the supplied evidence for factual healthcare claims.\n");
        prompt.append("- Do NOT invent sources or fabricate citations.\n");
        prompt.append("- Do NOT claim certainty when the evidence is insufficient; say so explicitly.\n");
        prompt.append("- Do NOT provide a diagnosis or prescribe medication.\n");
        prompt.append("- Do NOT invent patient-specific facts.\n");
        prompt.append("- Clearly distinguish general educational information from any personalised context provided.\n");
        prompt.append("- Reference evidence using the citation indices provided, e.g. [1], [2].\n");
        prompt.append("- When symptoms could be serious, name warning signs and clearly say when to seek professional help.\n");
        prompt.append("- Write in simple, patient-friendly language.\n\n");

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("PREVIOUS CONVERSATION\n");
            for (String turn : conversationHistory) {
                prompt.append("- ").append(turn).append('\n');
            }
            prompt.append('\n');
        }

        if (patientContextSummary != null && !patientContextSummary.isBlank()) {
            prompt.append("PATIENT CONTEXT (authorised, use only as background, NOT as evidence for claims)\n");
            prompt.append(patientContextSummary).append("\n\n");
        }

        prompt.append("KNOWLEDGE-BASE EVIDENCE\n");
        if (!context.hasEvidence()) {
            prompt.append("(no evidence retrieved)\n\n");
        } else {
            prompt.append(context.getContextBlock()).append('\n');
        }

        prompt.append("PATIENT QUESTION\n");
        prompt.append(context.getQuery()).append("\n\n");

        prompt.append("RESPONSE FORMAT\n");
        prompt.append("Answer in 3-6 sentences. End with a 'Sources:' line listing the citation indices you used.\n");
        prompt.append("If the evidence is insufficient, respond with exactly: I don't have enough information in the available knowledge base to answer this reliably.\n");
        return prompt.toString();
    }

    public static String insufficientEvidenceAnswer() {
        return INSUFFICIENT_EVIDENCE_FALLBACK;
    }
}
