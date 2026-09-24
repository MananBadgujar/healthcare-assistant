package com.healthcare.assistant.rag;

import com.healthcare.assistant.entity.EducationConversation;
import com.healthcare.assistant.entity.EducationMessage;
import com.healthcare.assistant.rag.conversation.EducationConversationService;
import com.healthcare.assistant.rag.conversation.RagAssistantPayload;
import com.healthcare.assistant.rag.conversation.UnauthorizedConversationException;
import com.healthcare.assistant.rag.conversation.UnknownConversationException;
import com.healthcare.assistant.rag.dto.AskRequest;
import com.healthcare.assistant.rag.dto.RagResponse;
import com.healthcare.assistant.rag.generation.GroundedPromptBuilder;
import com.healthcare.assistant.rag.patient.PatientContextSummaryBuilder;
import com.healthcare.assistant.rag.retrieval.RetrievedEvidence;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.safety.RagSafetyGuard;
import com.healthcare.assistant.rag.safety.RagSafetyGuard.SafetyDecision;
import com.healthcare.assistant.rag.conversation.CitationJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service-level facade binding the RAG pipeline to the conversational education
 * store and the safety guard. The ask controllers delegate here so the
 * security, safety, retrieval, LLM, citation validation and persistence
 * orchestration stays in one place.
 */
@Service
public class PatientEducationFacade {

    private static final Logger log = LoggerFactory.getLogger(PatientEducationFacade.class);

    private final SemanticSearchService searchService;
    private final RagOrchestrator orchestrator;
    private final RagSafetyGuard safetyGuard;
    private final PatientContextSummaryBuilder patientContextSummaryBuilder;
    private final EducationConversationService conversationService;

    public PatientEducationFacade(SemanticSearchService searchService,
                                  RagOrchestrator orchestrator,
                                  RagSafetyGuard safetyGuard,
                                  PatientContextSummaryBuilder patientContextSummaryBuilder,
                                  EducationConversationService conversationService) {
        this.searchService = searchService;
        this.orchestrator = orchestrator;
        this.safetyGuard = safetyGuard;
        this.patientContextSummaryBuilder = patientContextSummaryBuilder;
        this.conversationService = conversationService;
    }

    /**
     * Answer a patient-education question.
     *
     * @param request the validated ask request
     * @return structured RAG response (also persists the conversation)
     */
    public RagResponse ask(AskRequest request) {
        String principal = currentPrincipal();

        // 1. Safety guard: emergency escalation + health-domain restriction +
        //    medication safety. Short-circuits the entire pipeline.
        SafetyDecision safety = safetyGuard.evaluate(request.getQuestion());
        if (!safety.isProceed()) {
            log.info("RAG safety decision code={} for principal={}", safety.getReasonCode(), principal);
            return safetyResponse(safety, request, principal);
        }

        // 2. Retrieve evidence.
        RetrievedEvidence evidence = searchService.retrieve(request.getQuestion(), request.getCategory());

        // 3. Resolve or create a conversation so the user message precedes the
        //    LLM call, and history (excluding the just-appended user message)
        //    is bounded and fed to the LLM as context.
        EducationConversation conversation;
        EducationMessage userMessage;
        try {
            conversation = conversationService.resolveOrCreate(request.getConversationId(), principal);
            userMessage = conversationService.appendUserMessage(conversation, request.getQuestion());
        } catch (UnknownConversationException | UnauthorizedConversationException ex) {
            log.warn("Conversation resolution failed: {}", ex.getMessage());
            return RagResponse.ungroundedFallback(ex.getMessage(), GroundedPromptBuilder.DISCLAIMER);
        }
        java.util.List<String> history = conversationService.boundedHistory(conversation, userMessage);

        // 4. Optional patient context (only when authorised & permitted).
        String patientContext = request.isIncludePatientContext()
                ? patientContextSummaryBuilder.build() : "";

        // 5. Orchestrate retrieval -> LLM -> citation validation.
        RagResponse response = orchestrator.answer(evidence, patientContext, history);
        response.setConversationId(conversation.getExternalConversationId());

        // 6. Persist assistant message + citations.
        conversationService.appendAssistantMessage(conversation, new RagAssistantPayload(
                response.getAnswer(),
                CitationJsonSerializer.serialise(response),
                response.isGrounded(),
                response.isRequiresProviderReview(),
                response.getConfidence()));
        return response;
    }

    private RagResponse safetyResponse(SafetyDecision safety, AskRequest request, String principal) {
        RagResponse response = new RagResponse();
        response.setAnswer(safety.getMessage());
        response.setSources(Collections.emptyList());
        response.setConfidence("NONE");
        response.setGrounded(false);
        response.setRequiresProviderReview(true);
        response.setDisclaimer(GroundedPromptBuilder.DISCLAIMER);
        // Safety decisions skip retrieval; we still (optionally) record the
        // user's question so the conversation history reflects the exchange.
        try {
            EducationConversation conversation =
                    conversationService.resolveOrCreate(request.getConversationId(), principal);
            conversationService.appendUserMessage(conversation, request.getQuestion());
            response.setConversationId(conversation.getExternalConversationId());
            conversationService.appendAssistantMessage(conversation, new RagAssistantPayload(
                    safety.getMessage(), "[]", false, true, "NONE"));
        } catch (RuntimeException ex) {
            log.warn("Could not persist safety conversation: {}", ex.getMessage());
        }
        return response;
    }

    private String currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getName() != null
                ? authentication.getName() : "anonymous";
    }
}
