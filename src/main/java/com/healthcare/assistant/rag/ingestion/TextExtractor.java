package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.entity.enums.KbDocumentFormat;

import java.util.EnumSet;
import java.util.Set;

/**
 * Strategy for extracting clean text from a raw knowledge-base document.
 * <p>
 * Each supported {@link KbDocumentFormat} has a dedicated extractor
 * implementation. Extractors must return non-null cleaned text or throw an
 * extraction failure exception so the ingestion pipeline can surface the error
 * rather than silently dropping the document.
 */
public interface TextExtractor {

    /**
     * Whether this extractor handles the given format.
     *
     * @param format source document format
     * @return true if this extractor can handle the format
     */
    default boolean supports(KbDocumentFormat format) {
        return supportsFormats().contains(format);
    }

    /**
     * Formats supported by this extractor. Used by the registry to build the
     * format -> extractor index at startup.
     *
     * @return non-empty set of formats
     */
    default Set<KbDocumentFormat> supportsFormats() {
        return EnumSet.noneOf(KbDocumentFormat.class);
    }

    /**
     * Extract raw text from the supplied payload.
     *
     * @param payload raw bytes of the document
     * @return cleaned text, never {@code null}
     * @throws ExtractionException if extraction fails
     */
    String extract(byte[] payload, String explicitText);
}
