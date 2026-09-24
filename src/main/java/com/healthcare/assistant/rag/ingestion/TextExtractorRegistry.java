package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the correct {@link TextExtractor} for a given document format.
 * <p>
 * All {@link TextExtractor} beans in the application context are collected at
 * startup and indexed by their supported formats. Extractors may support more
 * than one format; resolution picks the first bean that claims to support the
 * requested format.
 */
@Component
public class TextExtractorRegistry {

    private final Map<KbDocumentFormat, TextExtractor> extractorsByFormat;

    public TextExtractorRegistry(List<TextExtractor> extractors) {
        this.extractorsByFormat = extractors.stream()
                .flatMap(extractor -> extractor.supportsFormats().stream()
                        .map(format -> java.util.Map.entry(format, (TextExtractor) extractor)))
                .collect(Collectors.toMap(java.util.Map.Entry::getKey,
                        java.util.Map.Entry::getValue,
                        (existing, replacement) -> existing));
    }

    public TextExtractor resolve(KbDocumentFormat format) {
        TextExtractor extractor = extractorsByFormat.get(format);
        if (extractor == null) {
            throw new ExtractionException("Unsupported document format: " + format);
        }
        return extractor;
    }
}
