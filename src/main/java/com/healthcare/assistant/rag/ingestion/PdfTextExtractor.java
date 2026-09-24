package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Extractor for PDF documents using Apache PDFBox.
 * <p>
 * The extractor reads the document from the raw byte payload, strips text page
 * by page (preserving page boundaries which the chunker uses to attach page
 * numbers to chunks), and fails with a typed {@link ExtractionException} for
 * malformed or empty PDFs.
 * <p>
 * The page-by-page extraction returns a list of {@link PageText} entries so the
 * downstream ingestion pipeline can record the originating page of each chunk
 * in {@code KbChunk.page}. The {@link #extract(byte[], String)} contract still
 * returns a single concatenated string for compatibility with non-chunk-aware
 * callers; the page-aware contract is exposed via
 * {@link #extractByPage(byte[], String)}.
 */
@Component
public class PdfTextExtractor implements TextExtractor {

    /** Marker format for inserting page boundaries into the concatenated text. */
    private static final String PAGE_SEPARATOR = "\n\n\f\n\n";

    @Override
    public Set<KbDocumentFormat> supportsFormats() {
        return EnumSet.of(KbDocumentFormat.PDF);
    }

    @Override
    public String extract(byte[] payload, String explicitText) {
        if (explicitText != null && !explicitText.isBlank()) {
            return explicitText.trim();
        }
        if (payload == null || payload.length == 0) {
            throw new ExtractionException("PDF payload is empty; cannot extract text");
        }
        List<PageText> pages = extractByPage(payload, explicitText);
        if (pages.isEmpty()) {
            throw new ExtractionException("PDF contains no extractable text (possible scanned image)");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            if (i > 0) {
                sb.append(PAGE_SEPARATOR);
            }
            sb.append(pages.get(i).text());
        }
        return sb.toString().trim();
    }

    /**
     * Extract text page by page from a PDF payload.
     * <p>
     * Returns an ordered list, one {@link PageText} per PDF page, so the
     * ingestion service can assign {@code KbChunk.page} when persisting chunks.
     * If the caller supplied pre-extracted text via {@code explicitText} the
     * entire text is returned as a single virtual page (page = 1) so legacy
     * callers do not break.
     *
     * @param payload      raw PDF bytes; required when explicitText is null/blank
     * @param explicitText optional pre-extracted text; honoured in preference to payload
     * @return ordered per-page text fragments (never null, never empty when input is valid)
     * @throws ExtractionException for malformed or empty PDFs
     */
    public List<PageText> extractByPage(byte[] payload, String explicitText) {
        if (explicitText != null && !explicitText.isBlank()) {
            List<PageText> virtual = new ArrayList<>();
            virtual.add(new PageText(1, explicitText.trim()));
            return virtual;
        }
        if (payload == null || payload.length == 0) {
            throw new ExtractionException("PDF payload is empty; cannot extract text");
        }
        List<PageText> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(payload)) {
            int pageCount = document.getNumberOfPages();
            boolean anyText = false;
            for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                String text = stripper.getText(document);
                if (text != null) {
                    String normalised = text.replace("\r\n", "\n").trim();
                    pages.add(new PageText(pageNumber, normalised));
                    if (!normalised.isBlank()) {
                        anyText = true;
                    }
                } else {
                    pages.add(new PageText(pageNumber, ""));
                }
            }
            if (!anyText) {
                throw new ExtractionException("PDF contains no extractable text (possible scanned image)");
            }
            return pages;
        } catch (IOException e) {
            throw new ExtractionException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Per-page extraction result.
     */
    public static final class PageText {
        private final int pageNumber;
        private final String text;

        public PageText(int pageNumber, String text) {
            this.pageNumber = pageNumber;
            this.text = text == null ? "" : text;
        }

        public int pageNumber() {
            return pageNumber;
        }

        public String text() {
            return text;
        }
    }
}
