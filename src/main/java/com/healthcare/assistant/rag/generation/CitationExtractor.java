package com.healthcare.assistant.rag.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the citation indices referenced in an LLM answer.
 * <p>
 * Citations appear as bracketed integers, e.g. {@code [1]}, {@code [2]}. Both
 * inline references and a trailing {@code Sources:} line are scanned; duplicate
 * references for the same index are collapsed so the validator sees a deduped
 * list.
 */
public final class CitationExtractor {

    private static final Pattern CITATION = Pattern.compile("\\[(\\d{1,2})\\]");

    private CitationExtractor() {
    }

    public static List<Integer> extract(String answer) {
        List<Integer> citations = new ArrayList<>();
        if (answer == null || answer.isBlank()) {
            return citations;
        }
        Matcher matcher = CITATION.matcher(answer);
        while (matcher.find()) {
            try {
                int idx = Integer.parseInt(matcher.group(1));
                if (idx >= 1 && !citations.contains(idx)) {
                    citations.add(idx);
                }
            } catch (NumberFormatException ignored) {
                // defensive: integer regex makes this unreachable
            }
        }
        return citations;
    }
}
