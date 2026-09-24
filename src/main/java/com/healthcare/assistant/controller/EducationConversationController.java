package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.EducationConversation;
import com.healthcare.assistant.entity.EducationFeedback;
import com.healthcare.assistant.entity.EducationMessage;
import com.healthcare.assistant.rag.conversation.EducationConversationService;
import com.healthcare.assistant.rag.conversation.UnauthorizedConversationException;
import com.healthcare.assistant.rag.conversation.UnknownConversationException;
import com.healthcare.assistant.rag.dto.ConversationDto;
import com.healthcare.assistant.rag.dto.FeedbackRequest;
import com.healthcare.assistant.rag.feedback.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * History and feedback endpoints for the conversational patient-education flow.
 * All endpoints are JWT-authenticated by the global security configuration and
 * are scoped to the authenticated principal.
 */
@RestController
@RequestMapping("/api/v1/kb")
public class EducationConversationController {

    private final EducationConversationService conversationService;
    private final FeedbackService feedbackService;

    public EducationConversationController(EducationConversationService conversationService,
                                          FeedbackService feedbackService) {
        this.conversationService = conversationService;
        this.feedbackService = feedbackService;
    }

    /** List the caller's conversations. */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> listConversations() {
        String principal = currentPrincipal();
        List<ConversationDto> dtos = conversationService.conversationsFor(principal).stream()
                .map(conversation -> new ConversationDto(conversation, conversation.getMessages()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Get all messages of a conversation (with citations and feedback). */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable String conversationId) {
        String principal = currentPrincipal();
        EducationConversation conversation = conversationService.resolveOrCreate(conversationId, principal);
        List<EducationMessage> messages = conversationService.history(conversation);
        return ResponseEntity.ok(new ConversationDto(conversation, messages));
    }

    /** Submit feedback for a specific assistant message. */
    @PostMapping("/feedback/{messageId}")
    public ResponseEntity<?> feedback(@PathVariable Long messageId,
                                      @RequestBody FeedbackRequest request) {
        if (request == null || request.getCategory() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "category is required"));
        }
        EducationFeedback saved = feedbackService.submit(
                messageId, request.getCategory(), request.getComment(), currentPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ConversationDto.FeedbackDto(saved));
    }

    @ExceptionHandler(UnknownConversationException.class)
    public ResponseEntity<Map<String, String>> handleUnknown(UnknownConversationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedConversationException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedConversationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    private String currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getName() != null
                ? authentication.getName() : "anonymous";
    }
}
