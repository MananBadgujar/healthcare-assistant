package com.healthcare.assistant.rag.conversation;

/**
 * Raised when the caller attempts to access an education conversation that
 * does not belong to the authenticated principal.
 */
public class UnauthorizedConversationException extends RuntimeException {
    public UnauthorizedConversationException(String message) {
        super(message);
    }
}
