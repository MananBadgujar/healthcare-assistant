package com.healthcare.assistant.rag;

import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import com.healthcare.assistant.rag.dto.SearchHitDto;
import com.healthcare.assistant.rag.ingestion.DocumentIngestionService;
import com.healthcare.assistant.rag.ingestion.IngestDocumentRequest;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.vector.VectorRecord;
import com.healthcare.assistant.repository.KbChunkRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying PDF ingestion populates the originating page
 * number on each chunk and that this metadata is exposed through the
 * semantic-search / citation contract.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PdfIngestionIntegrationTest {

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private SemanticSearchService searchService;

    @Autowired
    private KbChunkRepository chunkRepository;

    @Test
    void pdfIngestionAttachesPageNumberToEveryChunk() throws Exception {
        byte[] pdf = buildPdf(List.of(
                "Type 2 diabetes is a chronic condition that affects how the body processes blood glucose. "
                        + "Insulin resistance is a key feature of this condition.",
                "Regular physical activity and a balanced diet help manage diabetes and blood glucose levels.",
                "Patients should consult their healthcare provider before adjusting any diabetes medication."));

        IngestDocumentRequest request = new IngestDocumentRequest();
        request.setExternalId("pdf-diabetes-guide");
        request.setName("PDF Diabetes Guide");
        request.setSource("Clinic education unit");
        request.setCategory(KbCategory.CHRONIC_CONDITIONS);
        request.setTopic("Type 2 diabetes");
        request.setVersion("1");
        request.setFormat(KbDocumentFormat.PDF);
        request.setPayload(pdf);

        ingestionService.ingest(request);

        var chunks = chunkRepository.findByDocumentId(
                ingestionService.findByExternalIdAndVersion("pdf-diabetes-guide", "1").getId());
        assertFalse(chunks.isEmpty(), "PDF ingestion must produce chunks");
        for (var chunk : chunks) {
            assertNotNull(chunk.getPage(), "PDF chunk must carry the originating page number");
            assertTrue(chunk.getPage() >= 1 && chunk.getPage() <= 3,
                    "page number must be within the PDF range; was " + chunk.getPage());
        }
    }

    @Test
    void searchHitsCarryPageMetadataForPdfChunks() throws Exception {
        byte[] pdf = buildPdf(List.of(
                "Hypertension is a chronic condition characterized by elevated blood pressure in the arteries.",
                "Lifestyle changes including reduced sodium intake and regular exercise help manage hypertension."));

        IngestDocumentRequest request = new IngestDocumentRequest();
        request.setExternalId("pdf-hypertension");
        request.setName("PDF Hypertension Guide");
        request.setSource("Cardiac clinic");
        request.setCategory(KbCategory.CHRONIC_CONDITIONS);
        request.setTopic("Hypertension");
        request.setVersion("1");
        request.setFormat(KbDocumentFormat.PDF);
        request.setPayload(pdf);
        ingestionService.ingest(request);

        List<VectorRecord> hits = searchService.search("hypertension blood pressure", null);
        assertFalse(hits.isEmpty(), "expected retrieval to find PDF chunks");
        boolean sawPdf = hits.stream().anyMatch(h -> "pdf-hypertension".equals(h.getExternalId()));
        assertTrue(sawPdf, "expected a PDF chunk in the retrieval results");
        // At least one hit must carry a non-null page number.
        assertTrue(hits.stream().anyMatch(h -> h.getPage() != null),
                "PDF citation hits must expose the originating page number");
    }

    private byte[] buildPdf(List<String> pageTexts) throws Exception {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(50, 700);
                    // Wrap long text into lines so the page does not overflow.
                    for (String line : wrap(text, 70)) {
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

    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > width && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else if (current.length() == 0) {
                current.append(word);
            } else {
                current.append(' ').append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
