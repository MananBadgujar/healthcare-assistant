package com.healthcare.assistant.rag.retrieval;

import com.healthcare.assistant.rag.config.RagProperties;
import com.healthcare.assistant.rag.vector.VectorRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a grounded context block from retrieved evidence for the LLM prompt.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>rank hits by similarity (already sorted by the vector store);</li>
 *   <li>remove duplicate chunk text that can appear when an older and a newer
 *       version of a document share text (de-duplication by content);</li>
 *   <li>respect the configured maximum context size in characters;</li>
 *   <li>assign a stable, 1-based citation index to each retained chunk so the
 *       LLM can reference sources deterministically (e.g. "[1]");</li>
 *   <li>produce a {@link RagContext} containing the rendered context block, the
 *       citation list and the underlying hits for the citation validator.</li>
 * </ul>
 * The builder never truncates a chunk; if a chunk does not fit into the
 * remaining budget it is dropped rather than partially included, so the LLM
 * always sees complete evidence snippets.
 */
@Component
public class RagContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(RagContextBuilder.class);

    private final RagProperties properties;

    public RagContextBuilder(RagProperties properties) {
        this.properties = properties;
    }

    public RagContext build(RetrievedEvidence evidence) {
        if (evidence == null || !evidence.hasEvidence()) {
            return RagContext.empty(evidence == null ? "" : evidence.getQuery());
        }
        int maxChars = Math.max(512, properties.getGeneration().getMaxContextChars());
        Set<String> seenText = new HashSet<>();
        List<VectorRecord> retained = new ArrayList<>();
        List<Citation> citations = new ArrayList<>();
        StringBuilder block = new StringBuilder();
        int index = 1;
        int used = 0;
        for (VectorRecord hit : evidence.getHits()) {
            String normalised = normaliseForDedup(hit.getText());
            if (!seenText.add(normalised)) {
                continue;
            }
            String header = String.format("[Citation %d] Source: %s | Section: %s | Page: %s | Version: %s%n",
                    index,
                    nonNull(hit.getDocumentName()),
                    nonNull(hit.getSection()),
                    hit.getPage() == null ? "n/a" : hit.getPage(),
                    nonNull(hit.getVersion()));
            String snippet = hit.getText().strip() + System.lineSeparator();
            int needed = header.length() + snippet.length();
            if (used + needed > maxChars) {
                break;
            }
            block.append(header).append(snippet);
            retained.add(hit);
            citations.add(new Citation(index,
                    hit.getDocumentName(),
                    hit.getExternalId(),
                    hit.getSection(),
                    hit.getPage(),
                    hit.getVersion(),
                    hit.getTopic(),
                    hit.getCategory(),
                    hit.getSimilarity()));
            used += needed;
            index++;
        }
        log.debug("Built RAG context: {} citations, {} chars used", citations.size(), used);
        return new RagContext(evidence.getQuery(), block.toString(), citations, retained);
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }

    private static String normaliseForDedup(String text) {
        return text == null ? "" : text.strip().toLowerCase(java.util.Locale.ROOT);
    }
}
