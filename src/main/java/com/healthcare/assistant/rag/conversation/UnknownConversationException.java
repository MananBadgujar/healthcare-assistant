package com.healthcare.assistant.rag.conversation;

/**
 * Raised when an education conversation cannot be found by its external id.
 */
public class UnknownConversationException extends RuntimeException {
    public UnknownConversationException(String message) {
        super(message);
    }
}
