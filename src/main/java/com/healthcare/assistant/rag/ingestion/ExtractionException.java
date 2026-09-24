package com.healthcare.assistant.rag.ingestion;

/**
 * Raised when document text extraction fails (malformed PDF, unsupported
 * encoding, empty payload, oversized document, etc.). Throwing a typed
 * exception ensures the ingestion service can return a clean API response
 * instead of letting low-level I/O failures surface to the client.
 */
public class ExtractionException extends RuntimeException {

    public ExtractionException(String message) {
        super(message);
    }

    public ExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
