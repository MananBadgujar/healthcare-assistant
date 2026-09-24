package com.healthcare.assistant.rag.ingestion;

import java.util.Objects;

/**
 * Immutable result of chunking a document. A {@code Chunk} carries the chunk
 * text plus the metadata that can be derived purely from the document text and
 * chunking parameters (sequence index, optional section, optional page). The
 * originating document, source, category, version and active flag are attached
 * later by the ingestion service when the chunk is persisted, so the chunker
 * itself stays format-agnostic.
 */
public final class Chunk {

    private final String text;
    private final int sequence;
    private final String section;
    private final Integer page;

    public Chunk(String text, int sequence, String section, Integer page) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Chunk text must not be blank");
        }
        if (sequence < 0) {
            throw new IllegalArgumentException("Chunk sequence must be non-negative");
        }
        this.text = text;
        this.sequence = sequence;
        this.section = section == null ? "" : section;
        this.page = page;
    }

    public String text() {
        return text;
    }

    public int sequence() {
        return sequence;
    }

    public String section() {
        return section;
    }

    public Integer page() {
        return page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;
        Chunk other = (Chunk) o;
        return sequence == other.sequence && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, sequence);
    }

    @Override
    public String toString() {
        return "Chunk{sequence=" + sequence + ", section='" + section + "'}";
    }
}
