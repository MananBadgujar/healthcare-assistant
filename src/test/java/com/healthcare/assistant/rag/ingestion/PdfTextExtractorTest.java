package com.healthcare.assistant.rag.ingestion;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PdfTextExtractor}. Generates small in-memory PDFs with
 * known text content to verify per-page extraction.
 */
class PdfTextExtractorTest {

    private byte[] buildPdf(List<String> pageTexts) throws Exception {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(50, 700);
                    for (String line : text.split("\\n")) {
                        stream.showText(line);
                        stream.newLineAtOffset(0, -16);
                    }
                    stream.endText();
                }
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void extractReturnsConcatenatedText() throws Exception {
        byte[] pdf = buildPdf(List.of(
                "Diabetes is a chronic condition.",
                "Regular exercise helps management."));
        PdfTextExtractor extractor = new PdfTextExtractor();
        String text = extractor.extract(pdf, null);
        assertTrue(text.toLowerCase().contains("diabetes"));
        assertTrue(text.toLowerCase().contains("exercise"));
    }

    @Test
    void extractByPageReturnsOrderedPagesWithCorrectNumbers() throws Exception {
        byte[] pdf = buildPdf(List.of(
                "Page one content about diabetes.",
                "Page two content about exercise."));
        PdfTextExtractor extractor = new PdfTextExtractor();
        List<PdfTextExtractor.PageText> pages = extractor.extractByPage(pdf, null);
        assertEquals(2, pages.size());
        assertEquals(1, pages.get(0).pageNumber());
        assertEquals(2, pages.get(1).pageNumber());
        assertTrue(pages.get(0).text().toLowerCase().contains("diabetes"));
        assertTrue(pages.get(1).text().toLowerCase().contains("exercise"));
    }

    @Test
    void extractByPageHandlesBlankPageGracefully() throws Exception {
        // Build a 3-page PDF where page 2 contains only a header line.
        byte[] pdf = buildPdf(List.of(
                "Real content page one about diabetes care.",
                "Header only",
                "Real content page three about nutrition."));
        PdfTextExtractor extractor = new PdfTextExtractor();
        List<PdfTextExtractor.PageText> pages = extractor.extractByPage(pdf, null);
        assertEquals(3, pages.size());
        assertEquals(1, pages.get(0).pageNumber());
        assertEquals(3, pages.get(2).pageNumber());
    }

    @Test
    void extractByPageRejectsEmptyPayload() {
        PdfTextExtractor extractor = new PdfTextExtractor();
        assertThrows(ExtractionException.class, () -> extractor.extractByPage(new byte[0], null));
        assertThrows(ExtractionException.class, () -> extractor.extractByPage(null, null));
    }

    @Test
    void extractByPagePrefersExplicitTextWhenProvided() {
        PdfTextExtractor extractor = new PdfTextExtractor();
        List<PdfTextExtractor.PageText> pages = extractor.extractByPage(null, "Pre-extracted educational text.");
        assertEquals(1, pages.size());
        assertEquals(1, pages.get(0).pageNumber());
        assertEquals("Pre-extracted educational text.", pages.get(0).text());
    }

    @Test
    void extractFailsOnMalformedPdfBytes() {
        PdfTextExtractor extractor = new PdfTextExtractor();
        assertThrows(ExtractionException.class, () -> extractor.extract("not a pdf".getBytes(), null));
    }
}
