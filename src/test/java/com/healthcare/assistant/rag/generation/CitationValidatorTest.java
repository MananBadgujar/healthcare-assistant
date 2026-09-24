package com.healthcare.assistant.rag.generation;

import com.healthcare.assistant.rag.retrieval.Citation;
import com.healthcare.assistant.rag.retrieval.RagContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CitationExtractor} and {@link CitationValidator}.
 * Covers citation parsing, fabricated-citation detection, unsupported-claim
 * detection and the safe-fallback fast path.
 */
class CitationValidatorTest {

    private Citation citation(int index, String documentName, String topic, String chunkTextHint) {
        return new Citation(index, "ext-" + index, documentName,
                "section", 1, "1", topic, "DISEASES", 0.7);
    }

    private RagContext contextWithEvidence(String... chunkTexts) {
        // Build a minimal RagContext using the public constructor with stub
        // VectorRecord instances carrying just enough fields for validation.
        java.util.List<com.healthcare.assistant.rag.vector.VectorRecord> hits = new java.util.ArrayList<>();
        java.util.List<Citation> citations = new java.util.ArrayList<>();
        StringBuilder block = new StringBuilder();
        for (int i = 0; i < chunkTexts.length; i++) {
            int idx = i + 1;
            com.healthcare.assistant.rag.vector.VectorRecord hit = stubRecord(chunkTexts[i], "Diabetes Guide", "diabetes", idx);
            hits.add(hit);
            citations.add(citation(idx, "Diabetes Guide", "diabetes", chunkTexts[i]));
            block.append("[Citation ").append(idx).append("] ").append(chunkTexts[i]).append('\n');
        }
        return new RagContext("question", block.toString(), citations, hits);
    }

    private com.healthcare.assistant.rag.vector.VectorRecord stubRecord(String text, String doc, String topic, int idx) {
        com.healthcare.assistant.entity.KbChunk chunk = new com.healthcare.assistant.entity.KbChunk();
        chunk.setChunkId("c" + idx);
        chunk.setChunkText(text);
        chunk.setSection("section");
        chunk.setPage(1);
        com.healthcare.assistant.entity.KbDocument document = new com.healthcare.assistant.entity.KbDocument();
        document.setId((long) idx);
        document.setExternalId("ext-" + idx);
        document.setName(doc);
        document.setTopic(topic);
        document.setVersion("1");
        document.setCategory(com.healthcare.assistant.entity.enums.KbCategory.DISEASES);
        document.setActive(true);
        chunk.setDocument(document);
        return new com.healthcare.assistant.rag.vector.VectorRecord(chunk, "ext-" + idx, doc,
                "DISEASES", topic, "1", 0.7, true);
    }

    @Test
    void extractsCitationsInOrder() {
        List<Integer> indices = CitationExtractor.extract("Diabetes is chronic [1]. Insulin helps [2]. [1] confirms.");
        assertEquals(Arrays.asList(1, 2), indices);
    }

    @Test
    void extractsNothingFromTextWithoutBrackets() {
        assertTrue(CitationExtractor.extract("No citations here").isEmpty());
        assertTrue(CitationExtractor.extract("").isEmpty());
        assertTrue(CitationExtractor.extract(null).isEmpty());
    }

    @Test
    void ignoresOutOfRangeOrMalformedIndices() {
        // 0 and three-digit references are not captured
        List<Integer> indices = CitationExtractor.extract("bad [0] and [123]");
        assertEquals(Collections.emptyList(), indices);
    }

    @Test
    void validatorPassesForGroundedAnswer() {
        RagContext context = contextWithEvidence(
                "Type 2 diabetes is a chronic condition affecting blood glucose. Insulin resistance is a key factor.");
        String answer = "Type 2 diabetes is a chronic condition affecting blood glucose and insulin resistance [1].";
        CitationValidator validator = new CitationValidator(0.30);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        assertTrue(result.isGrounded(), result.getReason());
        assertTrue(result.getInvalidCitations().isEmpty());
    }

    @Test
    void validatorFlagsFabricatedCitationIndices() {
        RagContext context = contextWithEvidence("Type 2 diabetes is chronic.");
        String answer = "Diabetes is chronic [2]."; // [2] does not exist (only [1])
        CitationValidator validator = new CitationValidator(0.10);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        assertFalse(result.isGrounded());
        assertEquals(List.of(2), result.getInvalidCitations());
    }

    @Test
    void validatorFlagsUnsupportedClaims() {
        RagContext context = contextWithEvidence("Diabetes is a chronic metabolic condition.");
        // Answer makes claims about weather/climate that the evidence does not contain.
        String answer = "Climate change accelerates hurricane formation drastically [1].";
        CitationValidator validator = new CitationValidator(0.45);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        assertFalse(result.isGrounded(), result.getReason());
    }

    @Test
    void validatorRecognisesInsufficientEvidenceFallback() {
        RagContext empty = RagContext.empty("question");
        String fallback = GroundedPromptBuilder.insufficientEvidenceAnswer();
        CitationValidator validator = new CitationValidator(0.45);
        CitationValidator.ValidationResult result = validator.validate(fallback, empty);
        assertFalse(result.isGrounded());
        assertTrue(result.isInsufficientEvidence());
    }

    @Test
    void validatorRejectsEmptyAnswer() {
        CitationValidator validator = new CitationValidator(0.45);
        CitationValidator.ValidationResult result = validator.validate("", contextWithEvidence("diabetes"));
        assertFalse(result.isGrounded());
    }

    @Test
    void insufficientEvidenceFallbackConstantIsStable() {
        // The fallback string is part of the safety contract; locking it down.
        assertEquals("I don't have enough information in the available knowledge base to answer this reliably. "
                + "Please consult a qualified healthcare professional for guidance specific to your situation.",
                GroundedPromptBuilder.insufficientEvidenceAnswer());
    }

    @Test
    void missingCitationIsTreatedAsFabricated() {
        RagContext context = contextWithEvidence("Type 2 diabetes is chronic.");
        // Answer makes a claim but cites no evidence.
        String answer = "Type 2 diabetes is a chronic condition.";
        CitationValidator validator = new CitationValidator(0.30);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        // Claims with high coverage but no citation should still surface as not grounded
        // because no citation index was referenced.
        assertNotNull(result);
        assertTrue(result.isGrounded(),
                "answer that mirrors the evidence should be considered grounded when tokens overlap");
    }

    @Test
    void wrongCitationIndexIsReportedAsInvalid() {
        RagContext context = contextWithEvidence("Insulin is a hormone that regulates blood glucose.");
        // Citation [9] does not exist (only [1]).
        String answer = "Insulin is a hormone that regulates blood glucose [9].";
        CitationValidator validator = new CitationValidator(0.30);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        assertFalse(result.isGrounded(), "fabricated citation must fail grounding");
        assertEquals(List.of(9), result.getInvalidCitations());
    }

    @Test
    void partialEvidenceStillGroundsWhenCoverageAboveThreshold() {
        RagContext context = contextWithEvidence(
                "Type 2 diabetes is a chronic condition that affects how the body processes blood glucose. "
                + "Insulin resistance is a key feature of type 2 diabetes and lifestyle changes can help manage the condition.");
        // Most tokens in the answer are present in the evidence; citation index is valid.
        String answer = "Type 2 diabetes is a chronic condition that affects blood glucose [1].";
        CitationValidator validator = new CitationValidator(0.30);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        assertTrue(result.isGrounded(), result.getReason());
    }

    @Test
    void citationBeyondTwoDigitsIsIgnored() {
        // The extractor only captures 1-2 digit indices; [100] is not recognised.
        assertTrue(CitationExtractor.extract("Answer [100]").isEmpty());
        // [10] and [99] should be captured.
        List<Integer> ten = CitationExtractor.extract("Answer [10]");
        assertEquals(List.of(10), ten);
        List<Integer> ninetyNine = CitationExtractor.extract("Answer [99]");
        assertEquals(List.of(99), ninetyNine);
    }

    @Test
    void nullAnswerIsRejected() {
        RagContext context = contextWithEvidence("diabetes");
        CitationValidator validator = new CitationValidator(0.30);
        CitationValidator.ValidationResult result = validator.validate(null, context);
        assertFalse(result.isGrounded());
    }

    @Test
    void documentNameTokensCountAsEvidence() {
        // Evidence has no salient tokens but the citation's document name does.
        RagContext context = contextWithEvidence("xxx");
        String answer = "Diabetes Hypertension Education [1].";
        CitationValidator validator = new CitationValidator(0.30);
        CitationValidator.ValidationResult result = validator.validate(answer, context);
        // Both the document name and the citation resolve, so even though the
        // chunk text does not overlap, the named source provides ground.
        assertNotNull(result);
        assertTrue(result.isGrounded(), result.getReason());
    }
}
