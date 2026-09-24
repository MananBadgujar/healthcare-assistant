package com.healthcare.assistant.entity.enums;

/**
 * Supported source document formats for knowledge-base ingestion.
 * <p>
 * The pipeline is designed so new values can be added without redesigning the
 * document-processing flow; each format is handled by a dedicated
 * {@code TextExtractor}.
 */
public enum KbDocumentFormat {
    TXT,
    MARKDOWN,
    PDF,
    STRUCTURED
}
