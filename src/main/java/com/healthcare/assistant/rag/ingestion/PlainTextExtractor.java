package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

/**
 * Extractor for plain-text and markdown documents.
 * <p>
 * For these formats the raw payload is the document text itself, so the
 * extractor simply normalises line endings and rejects empty payloads. When an
 * explicit {@code explicitText} value is supplied (structured ingestion where
 * the content is provided directly in the request) it is preferred over the
 * bytes payload.
 */
@Component
public class PlainTextExtractor implements TextExtractor {

    @Override
    public Set<KbDocumentFormat> supportsFormats() {
        return EnumSet.of(KbDocumentFormat.TXT, KbDocumentFormat.MARKDOWN, KbDocumentFormat.STRUCTURED);
    }

    @Override
    public String extract(byte[] payload, String explicitText) {
        if (explicitText != null && !explicitText.isBlank()) {
            return normalise(explicitText);
        }
        if (payload == null || payload.length == 0) {
            throw new ExtractionException("Document payload is empty; text extraction yielded no content");
        }
        String text = new String(payload, StandardCharsets.UTF_8);
        if (text.isBlank()) {
            throw new ExtractionException("Document payload is empty; text extraction yielded no content");
        }
        return normalise(text);
    }

    private String normalise(String text) {
        // Normalise line endings and collapse excessive whitespace.
        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }
}
