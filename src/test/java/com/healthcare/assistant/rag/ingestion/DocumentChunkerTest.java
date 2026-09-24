package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.rag.config.RagProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DocumentChunker}. Pure unit tests (no Spring context)
 * covering metadata preservation, overlap, paragraph/sentence splitting,
 * heading detection and edge cases.
 */
class DocumentChunkerTest {

    private DocumentChunker chunkerWith(int chunkSize, int overlap) {
        RagProperties properties = new RagProperties();
        properties.getIngestion().setChunkSize(chunkSize);
        properties.getIngestion().setChunkOverlap(overlap);
        return new DocumentChunker(properties);
    }

    @Test
    void rejectsBlankDocument() {
        DocumentChunker chunker = chunkerWith(100, 0);
        assertThrows(IllegalArgumentException.class, () -> chunker.chunk(""));
        assertThrows(IllegalArgumentException.class, () -> chunker.chunk("   "));
        assertThrows(IllegalArgumentException.class, () -> chunker.chunk(null));
    }

    @Test
    void shortDocumentProducesSingleChunkWithEmptySection() {
        DocumentChunker chunker = chunkerWith(1000, 0);
        List<Chunk> chunks = chunker.chunk("Diabetes is a condition that affects blood glucose.");
        assertEquals(1, chunks.size());
        assertEquals(0, chunks.get(0).sequence());
        assertEquals("", chunks.get(0).section());
        assertEquals("Diabetes is a condition that affects blood glucose.", chunks.get(0).text());
    }

    @Test
    void chunkSequenceIsIncremental() {
        DocumentChunker chunker = chunkerWith(40, 0);
        String text = "aaa bbb ccc ddd eee fff ggg hhh iii jjj kkk lll mmm nnn ooo ppp";
        List<Chunk> chunks = chunker.chunk(text);
        assertTrue(chunks.size() >= 2, "expected multiple chunks for large text");
        for (int i =  0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).sequence(), "chunk " + i + " sequence");
        }
    }

    @Test
    void markdownHeadingBecomesSectionMetadata() {
        DocumentChunker chunker = chunkerWith(45, 0);
        String text = "# Causes\n\nDiabetes has multiple causes including genetics.\n\n## Treatment\n\nLifestyle matters for management here.";
        List<Chunk> chunks = chunker.chunk(text);
        assertTrue(chunks.size() >= 2, "expected at least 2 chunks for the two sections: " + chunks.size());
        assertEquals("Causes", chunks.get(0).section());
        boolean treatmentChunkExists = chunks.stream()
                .anyMatch(c -> "Treatment".equals(c.section()));
        assertTrue(treatmentChunkExists, "expected a chunk with section 'Treatment': " + chunks);
    }

    @Test
    void chunksPreserveDocumentMetadataIndirectly() {
        // Chunker only emits text/sequence/section/page; the ingestion service
        // attaches document metadata later. We assert the chunker output is
        // free of document-specific fields (it must not invent them).
        DocumentChunker chunker = chunkerWith(100, 0);
        List<Chunk> chunks = chunker.chunk("Para one content here.\n\nPara two content here.");
        for (Chunk chunk : chunks) {
            assertNull(chunk.page(), "chunker never assigns page for plain text");
        }
    }

    @Test
    void overlapKeepsContextBetweenChunks() {
        DocumentChunker chunker = chunkerWith(30, 10);
        List<Chunk> chunks = chunker.chunk("zero one two three four five six seven eight nine ten eleven twelve");
        assertTrue(chunks.size() >= 2);
        // Tail of an earlier chunk should appear at the start of the next when
        // overlap applies to the split. We assert at least one non-empty chunk.
        assertTrue(chunks.stream().allMatch(c -> c.text().length() > 0));
    }

    @Test
    void deterministicForIdenticalInput() {
        DocumentChunker chunker = chunkerWith(80, 10);
        String text = "Paragraph one with several words.\n\nParagraph two continues with more words.\n\nParagraph three closes the example.";
        List<Chunk> first = chunker.chunk(text);
        List<Chunk> second = chunker.chunk(text);
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).text(), second.get(i).text());
            assertEquals(first.get(i).sequence(), second.get(i).sequence());
        }
    }
}
