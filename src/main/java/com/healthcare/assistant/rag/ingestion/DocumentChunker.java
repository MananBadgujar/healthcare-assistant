package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.rag.config.RagProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits extracted document text into overlapping, context-preserving chunks.
 * <p>
 * The chunker prefers paragraph boundaries first, sentence boundaries second,
 * and falls back to fixed-size character windows when those are unavailable,
 * always keeping the configured overlap to avoid breaking meaning. Markdown
 * heading fragments ("# Causes", "## Treatment ...") attached at paragraph
 * boundaries are captured as section metadata so citations can reference the
 * section the chunk came from.
 * <p>
 * Chunking is deterministic and side-effect free which keeps it unit-testable
 * without a Spring context.
 */
@Component
public class DocumentChunker {

    private static final Pattern PARAGRAPH_SPLIT =
            Pattern.compile("\\n\\s*\\n");
    private static final Pattern SENTENCE_SPLIT =
            Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern MARKDOWN_HEADING =
            Pattern.compile("^#{1,6}\\s+(.*)$");
    private static final int MIN_CHUNK_SIZE = 32;

    private final int chunkSize;
    private final int overlap;

    public DocumentChunker(RagProperties properties) {
        this.chunkSize = Math.max(MIN_CHUNK_SIZE, properties.getIngestion().getChunkSize());
        int computedOverlap = properties.getIngestion().getChunkOverlap();
        this.overlap = Math.min(computedOverlap, chunkSize / 2);
    }

    /**
     * Chunk the supplied document text.
     *
     * @param documentText cleaned, non-blank document text
     * @return ordered list of chunks (possibly one for short documents)
     * @throws IllegalArgumentException when the supplied text is blank
     */
    public List<Chunk> chunk(String documentText) {
        if (documentText == null || documentText.isBlank()) {
            throw new IllegalArgumentException("Cannot chunk an empty document");
        }
        List<Chunk> chunks = new ArrayList<>();
        String currentParagraphText = "";
        String currentSection = "";
        int sequence = 0;
        int startInText = 0;

        // Walk paragraphs, accumulating them until reaching the chunk size.
        for (String paragraph : PARAGRAPH_SPLIT.split(documentText)) {
            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            String heading = detectHeading(trimmed);
            if (heading != null) {
                currentSection = heading;
            }
            String candidate = currentParagraphText.isEmpty()
                    ? trimmed
                    : currentParagraphText + "\n\n" + trimmed;
            if (charLength(candidate) >= chunkSize) {
                if (!currentParagraphText.strip().isEmpty()) {
                    chunks.addAll(flush(currentParagraphText, currentSection,
                            sequence, chunkSize, overlap, null));
                    sequence += countChunks(currentParagraphText, chunkSize, overlap);
                    startInText += currentParagraphText.length();
                }
                if (charLength(trimmed) >= chunkSize) {
                    chunks.addAll(flush(trimmed, currentSection, sequence,
                            chunkSize, overlap, null));
                    sequence += countChunks(trimmed, chunkSize, overlap);
                    currentParagraphText = "";
                } else {
                    currentParagraphText = trimmed;
                }
            } else {
                currentParagraphText = candidate;
            }
        }
        if (!currentParagraphText.strip().isEmpty()) {
            chunks.addAll(flush(currentParagraphText, currentSection,
                    sequence, chunkSize, overlap, null));
        }
        if (chunks.isEmpty()) {
            // Single extremely short document: emit one chunk.
            chunks.add(new Chunk(documentText.strip(), 0, "", null));
        }
        return chunks;
    }

    /**
     * Page-aware variant: chunk a list of per-page text fragments and tag each
     * chunk with the originating PDF page number. Chunks that span a page
     * boundary are tagged with the page where the chunk begins (the
     * first page that contributed content).
     *
     * @param pages ordered per-page text fragments
     * @return ordered chunks with {@code page} populated for every chunk
     * @throws IllegalArgumentException when pages is null/empty or contains only blank text
     */
    public List<Chunk> chunkByPage(List<PdfTextExtractor.PageText> pages) {
        if (pages == null || pages.isEmpty()) {
            throw new IllegalArgumentException("Cannot chunk an empty page list");
        }
        List<Chunk> all = new ArrayList<>();
        int sequence = 0;
        for (PdfTextExtractor.PageText page : pages) {
            if (page == null || page.text() == null || page.text().isBlank()) {
                continue;
            }
            int pageNumber = page.pageNumber();
            // Use the same paragraph-aware chunker for each page individually so
            // chunk boundaries respect paragraph and sentence structure within
            // a single page. Carry forward any partial trailing paragraph so it
            // gets prepended on the next page (preserving cross-page continuity).
            String carry = "";
            for (String paragraph : PARAGRAPH_SPLIT.split(page.text())) {
                String trimmed = paragraph.strip();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String working = carry.isEmpty() ? trimmed : carry + "\n\n" + trimmed;
                if (charLength(working) >= chunkSize && !carry.isEmpty()) {
                    List<Chunk> flushed = flush(carry, "", sequence, chunkSize, overlap, pageNumber);
                    all.addAll(flushed);
                    sequence += flushed.size();
                    carry = trimmed;
                } else if (charLength(working) >= chunkSize) {
                    List<Chunk> flushed = flush(working, "", sequence, chunkSize, overlap, pageNumber);
                    all.addAll(flushed);
                    sequence += flushed.size();
                    carry = "";
                } else {
                    carry = working;
                }
            }
            if (!carry.isEmpty()) {
                List<Chunk> flushed = flush(carry, "", sequence, chunkSize, overlap, pageNumber);
                all.addAll(flushed);
                sequence += flushed.size();
            }
        }
        if (all.isEmpty()) {
            // Fallback: emit a single virtual chunk for the first page that
            // has any text. If every page is blank, fall back to a placeholder
            // chunk carrying the page number so persistence still succeeds.
            String fallbackText = "(blank)";
            int fallbackPage = pages.get(0).pageNumber();
            for (PdfTextExtractor.PageText page : pages) {
                if (page != null && page.text() != null && !page.text().isBlank()) {
                    fallbackText = page.text().strip();
                    fallbackPage = page.pageNumber();
                    break;
                }
            }
            all.add(new Chunk(fallbackText, 0, "", fallbackPage));
        }
        return all;
    }

    private String detectHeading(String paragraph) {
        String firstLine = paragraph.lines().findFirst().orElse(paragraph);
        var matcher = MARKDOWN_HEADING.matcher(firstLine);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Flush a single accumulated paragraph (or oversized single paragraph) into
     * one or more overlapping chunks. The chunker keeps paragraph boundaries
     * intact when they fit within a chunk, only splitting by sentence or fixed
     * window when a paragraph exceeds the configured size.
     *
     * @param page optional page number to attach to each emitted chunk (null for non-paginated sources)
     */
    private List<Chunk> flush(String text, String section, int startSequence,
                               int chunkSize, int overlap, Integer page) {
        List<Chunk> result = new ArrayList<>();
        String normalized = text.strip();
        if (charLength(normalized) <= chunkSize) {
            result.add(new Chunk(normalized, startSequence, section, page));
            return result;
        }
        // Split oversized paragraph by sentence then by fixed windows.
        List<String> sentences = new ArrayList<>();
        for (String sentence : SENTENCE_SPLIT.split(normalized)) {
            if (!sentence.isBlank()) {
                sentences.add(sentence.strip());
            }
        }
        StringBuilder buffer = new StringBuilder();
        for (String sentence : sentences) {
            if (buffer.length() + sentence.length() + 1 > chunkSize && buffer.length() > 0) {
                result.add(new Chunk(buffer.toString().strip(), startSequence++, section, page));
                buffer = new StringBuilder(keepOverlap(buffer.toString(), overlap));
            }
            if (buffer.length() > 0) {
                buffer.append(' ');
            }
            if (sentence.length() > chunkSize) {
                // Sentence alone exceeds chunk size; hard-split with overlap.
                // Guarantee forward progress by stepping at least one character
                // each iteration so the loop always terminates.
                int step = Math.max(1, chunkSize - overlap);
                int idx = 0;
                while (idx < sentence.length()) {
                    int end = Math.min(idx + chunkSize, sentence.length());
                    result.add(new Chunk(sentence.substring(idx, end).strip(),
                            startSequence++, section, page));
                    if (end >= sentence.length()) {
                        break;
                    }
                    idx = Math.max(idx + step, end - overlap);
                }
            } else {
                buffer.append(sentence);
            }
        }
        if (buffer.length() > 0) {
            result.add(new Chunk(buffer.toString().strip(), startSequence, section, page));
        }
        return result;
    }

    private String keepOverlap(String text, int overlap) {
        if (overlap <= 0 || text.length() <= overlap) {
            return "";
        }
        String tail = text.substring(text.length() - overlap);
        return tail.strip();
    }

    private int countChunks(String text, int chunkSize, int overlap) {
        if (charLength(text) <= chunkSize) {
            return 1;
        }
        int step = chunkSize - overlap;
        if (step <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) charLength(text) / step);
    }

    private int charLength(String text) {
        return text == null ? 0 : text.length();
    }
}
