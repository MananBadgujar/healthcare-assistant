package com.healthcare.assistant.rag;

import com.healthcare.assistant.entity.KbDocument;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.rag.dto.AskRequest;
import com.healthcare.assistant.rag.dto.RagResponse;
import com.healthcare.assistant.rag.dto.SearchHitDto;
import com.healthcare.assistant.rag.ingestion.DocumentIngestionService;
import com.healthcare.assistant.rag.ingestion.IngestDocumentRequest;
import com.healthcare.assistant.rag.retrieval.SemanticSearchService;
import com.healthcare.assistant.rag.vector.VectorRecord;
import com.healthcare.assistant.service.AiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "alice@example.com", roles = "USER")
class RagPipelineIntegrationTest {

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private SemanticSearchService searchService;

    @Autowired
    private PatientEducationFacade facade;

    @Autowired
    private PatientRepository patientRepository;

    @MockBean
    private AiProvider aiProvider;

    @BeforeEach
    void seedKnowledgeBaseAndPatient() {
        IngestDocumentRequest diabetes = new IngestDocumentRequest();
        diabetes.setExternalId("diabetes-guide");
        diabetes.setName("Diabetes Education Guide");
        diabetes.setSource("Hospital education department");
        diabetes.setCategory(KbCategory.CHRONIC_CONDITIONS);
        diabetes.setTopic("Type 2 diabetes");
        diabetes.setVersion("1");
        diabetes.setFormat(KbDocumentFormat.STRUCTURED);
        diabetes.setExplicitText(
                "# What is diabetes\n\n"
                        + "Type 2 diabetes is a chronic condition that affects how the body processes blood glucose. "
                        + "Insulin resistance is a key feature and lifestyle changes can help manage the condition.\n\n"
                        + "# Management\n\n"
                        + "Regular physical activity and a balanced diet help manage blood glucose levels.");
        ingestionService.ingest(diabetes);

        IngestDocumentRequest exercise = new IngestDocumentRequest();
        exercise.setExternalId("lifestyle-guide");
        exercise.setName("Patient Lifestyle Guidelines");
        exercise.setSource("Clinic wellness program");
        exercise.setCategory(KbCategory.LIFESTYLE);
        exercise.setTopic("Physical activity");
        exercise.setVersion("1");
        exercise.setFormat(KbDocumentFormat.STRUCTURED);
        exercise.setExplicitText(
                "Regular physical activity helps improve insulin sensitivity and supports blood glucose "
                        + "management. Aim for consistent moderate exercise such as brisk walking.");
        ingestionService.ingest(exercise);

        Patient patient = new Patient();
        patient.setEmail("alice@example.com");
        patient.setFirstName("Alice");
        patient.setLastName("Smith");
        patient.setDateOfBirth("1980-01-01");
        patient.setGender("female");
        patientRepository.save(patient);
    }

    @Test
    void ingestionPersistsDocumentChunksAndEmbeddings() {
        List<SearchHitDto> hits = searchServiceToDtos(searchService.search("diabetes blood glucose", null));
        assertFalse(hits.isEmpty(), "expected retrieval to find diabetes-guide");
        assertTrue(hits.stream().anyMatch(h -> h.getDocument().equals("Diabetes Education Guide")),
                "expected Diabetes Education Guide in results: " + hits);
    }

    @Test
    void semanticSearchRespectsCategoryFilter() {
        List<SearchHitDto> allHits = searchServiceToDtos(searchService.search("physical activity", null));
        List<SearchHitDto> lifestyleHits = searchServiceToDtos(
                searchService.search("physical activity", KbCategory.LIFESTYLE.name()));
        // LIFESTYLE filter returns only lifestyle-guide content.
        assertTrue(lifestyleHits.stream().allMatch(h -> "LIFESTYLE".equals(h.getCategory())),
                "category filter must restrict results");
        // An unfiltered search should be a superset of the filtered query.
        assertTrue(allHits.size() >= lifestyleHits.size());
    }

    @Test
    void orchestratorReturnsGroundedAnswerWithSources() {
        when(aiProvider.generateResponse(anyString())).thenReturn(
                "Type 2 diabetes is a chronic condition affecting blood glucose and insulin resistance [1]. "
                        + "Regular physical activity helps manage the condition.");
        AskRequest ask = new AskRequest();
        ask.setQuestion("What is diabetes?");
        RagResponse response = facade.ask(ask);
        assertNotNull(response);
        assertTrue(response.isGrounded(), "answer should be grounded when LLM cites retrieved evidence");
        assertNotNull(response.getSources());
        assertFalse(response.getSources().isEmpty(), "grounded answer must include sources");
        assertNotNull(response.getConversationId(), "facade must return a conversation id");
        assertNotNull(response.getDisclaimer(), "disclaimer must always be present");
    }

    @Test
    void orchestratorFallsBackWhenLlmProducesUnsupportedClaims() {
        // LLM answers with climate-change claims that are not in the knowledge base.
        when(aiProvider.generateResponse(anyString())).thenReturn(
                "Climate change drastically accelerates hurricane formation globally [1].");
        AskRequest ask = new AskRequest();
        ask.setQuestion("What is diabetes?");
        RagResponse response = facade.ask(ask);
        assertNotNull(response);
        assertFalse(response.isGrounded(), "unsupported claims must not be marked grounded");
        assertTrue(response.isRequiresProviderReview(), "fallback must require provider review");
        assertEquals(GroundedFallbackContract.MESSAGE, response.getAnswer());
    }

    @Test
    void orchestratorFallsBackWhenLlmUnavailable() {
        when(aiProvider.generateResponse(anyString())).thenReturn(
                "Safety override: AI service currently unavailable.");
        AskRequest ask = new AskRequest();
        ask.setQuestion("What is diabetes?");
        RagResponse response = facade.ask(ask);
        assertFalse(response.isGrounded());
        assertTrue(response.getAnswer().contains("I don't have enough information"));
    }

    @Test
    void emergencyQuestionShortCircuitsWithEscalationMessage() {
        AskRequest ask = new AskRequest();
        ask.setQuestion("I have severe chest pain and can't breathe");
        RagResponse response = facade.ask(ask);
        assertFalse(response.isGrounded());
        assertEquals("NONE", response.getConfidence());
        assertTrue(response.getAnswer().toLowerCase().contains("emergency"),
                "emergency question must receive an emergency message: " + response.getAnswer());
    }

    @Test
    void outOfDomainQuestionIsRejected() {
        AskRequest ask = new AskRequest();
        ask.setQuestion("Tell me a joke about programming");
        RagResponse response = facade.ask(ask);
        assertFalse(response.isGrounded());
        assertTrue(response.getAnswer().toLowerCase().contains("healthcare")
                || response.getAnswer().toLowerCase().contains("rephrase"),
                "out-of-domain message must redirect the patient");
    }

    @Test
    void documentVersioningActivatesNewVersionAndDeactivatesOld() {
        IngestDocumentRequest v2 = new IngestDocumentRequest();
        v2.setExternalId("diabetes-guide");
        v2.setName("Diabetes Education Guide v2");
        v2.setSource("Hospital education department");
        v2.setCategory(KbCategory.CHRONIC_CONDITIONS);
        v2.setTopic("Type 2 diabetes");
        v2.setVersion("2");
        v2.setFormat(KbDocumentFormat.STRUCTURED);
        v2.setExplicitText(
                "# Updated diabetes information\n\n"
                        + "Type 2 diabetes is a chronic metabolic disorder. Updated guidance recommends regular "
                        + "glucose monitoring and lifestyle modification.");
        ingestionService.ingest(v2);

        KbDocument active = ingestionService.activateVersion("diabetes-guide", "2");
        assertTrue(active.isActive());
        // Old version now inactive
        KbDocument old = ingestionService.findByExternalIdAndVersion("diabetes-guide", "1");
        assertFalse(old.isActive(), "activating a new version must deactivate the old version");

        // Retrieval only returns the active version's chunks.
        List<SearchHitDto> hits = searchServiceToDtos(searchService.search("diabetes chronic", null));
        assertTrue(hits.stream().allMatch(h -> "2".equals(h.getVersion())),
                "retrieval must only return active version: " + hits);
    }

    private List<SearchHitDto> searchServiceToDtos(List<VectorRecord> hits) {
        java.util.List<SearchHitDto> dtos = new java.util.ArrayList<>();
        for (VectorRecord hit : hits) {
            dtos.add(new SearchHitDto(hit, 280));
        }
        return dtos;
    }

    /** Pin the fallback message text so the contract test stays stable. */
    private static final class GroundedFallbackContract {
        static final String MESSAGE =
                "I don't have enough information in the available knowledge base to answer this reliably. "
                        + "Please consult a qualified healthcare professional for guidance specific to your situation.";
    }
}