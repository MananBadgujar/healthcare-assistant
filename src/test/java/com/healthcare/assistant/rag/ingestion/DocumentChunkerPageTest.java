package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.rag.config.RagProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the page-aware chunking path used by PDF ingestion.
 */
class DocumentChunkerPageTest {

    private DocumentChunker chunkerWith(int chunkSize, int overlap) {
        RagProperties properties = new RagProperties();
        properties.getIngestion().setChunkSize(chunkSize);
        properties.getIngestion().setChunkOverlap(overlap);
        return new DocumentChunker(properties);
    }

    @Test
    void pageAwareChunkingAttachesOriginatingPage() {
        DocumentChunker chunker = chunkerWith(500, 0);
        List<PdfTextExtractor.PageText> pages = List.of(
                new PdfTextExtractor.PageText(1, "Diabetes is a chronic condition affecting blood glucose levels."),
                new PdfTextExtractor.PageText(2, "Regular physical activity and a balanced diet help manage diabetes."));
        List<Chunk> chunks = chunker.chunkByPage(pages);
        assertEquals(2, chunks.size());
        assertEquals(1, chunks.get(0).page());
        assertEquals(2, chunks.get(1).page());
        assertTrue(chunks.get(0).text().toLowerCase().contains("diabetes"));
    }

    @Test
    void pageAwareChunkingIsolatesPages() {
        DocumentChunker chunker = chunkerWith(40, 0);
        List<PdfTextExtractor.PageText> pages = List.of(
                new PdfTextExtractor.PageText(1, "alpha bravo charlie delta echo"),
                new PdfTextExtractor.PageText(2, "foxtrot golf hotel india juliet"));
        List<Chunk> chunks = chunker.chunkByPage(pages);
        assertTrue(chunks.size() >= 2);
        long page1Count = chunks.stream().filter(c -> c.page() != null && c.page() == 1).count();
        long page2Count = chunks.stream().filter(c -> c.page() != null && c.page() == 2).count();
        assertTrue(page1Count >= 1 && page2Count >= 1,
                "chunks must be tagged with their originating page; was: " + chunks);
    }

    @Test
    void pageAwareChunkingSkipsBlankPages() {
        DocumentChunker chunker = chunkerWith(500, 0);
        List<PdfTextExtractor.PageText> pages = List.of(
                new PdfTextExtractor.PageText(1, "First page with real content about diabetes management."),
                new PdfTextExtractor.PageText(2, "   \n   "),
                new PdfTextExtractor.PageText(3, "Third page discussing insulin therapy."));
        List<Chunk> chunks = chunker.chunkByPage(pages);
        assertEquals(2, chunks.size());
        assertEquals(1, chunks.get(0).page());
        assertEquals(3, chunks.get(1).page());
    }

    @Test
    void pageAwareChunkingFallsBackWhenAllPagesBlank() {
        DocumentChunker chunker = chunkerWith(500, 0);
        List<PdfTextExtractor.PageText> pages = List.of(
                new PdfTextExtractor.PageText(1, "   "),
                new PdfTextExtractor.PageText(2, "  \n  "));
        // The fallback emits a single virtual chunk tagged with the first page number.
        List<Chunk> chunks = chunker.chunkByPage(pages);
        assertEquals(1, chunks.size());
        assertEquals(1, chunks.get(0).page());
    }

    @Test
    void pageAwareChunkingRejectsEmptyList() {
        DocumentChunker chunker = chunkerWith(100, 0);
        assertThrows(IllegalArgumentException.class, () -> chunker.chunkByPage(List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunker.chunkByPage(null));
    }
}
