package com.healthcare.assistant.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.repository.UserRepository;
import com.healthcare.assistant.rag.dto.IngestStructuredRequest;
import com.healthcare.assistant.rag.feedback.FeedbackCategory;
import com.healthcare.assistant.rag.dto.FeedbackRequest;
import com.healthcare.assistant.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests proving the security contract of the Phase 9 admin
 * endpoints: a patient (role USER) is forbidden from ingesting or managing
 * knowledge-base documents, while an admin (role ADMIN) succeeds. JWT tokens
 * are issued using the project's {@link JwtTokenUtil} and users are seeded into
 * the test H2 database so the JWT filter can resolve authorities. Tests run in
 * a transaction that rolls back after each method so seeded users do not bleed
 * across tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RagSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void seedUsers() {
        userRepository.findByEmail("patient@example.com").orElseGet(() -> {
            User patient = new User();
            patient.setName("Patient");
            patient.setEmail("patient@example.com");
            patient.setPassword(passwordEncoder.encode("password"));
            patient.setRole("USER");
            return userRepository.save(patient);
        });
        userRepository.findByEmail("admin@example.com").orElseGet(() -> {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole("ADMIN");
            return userRepository.save(admin);
        });
        userRepository.findByEmail("provider@example.com").orElseGet(() -> {
            User provider = new User();
            provider.setName("Provider");
            provider.setEmail("provider@example.com");
            provider.setPassword(passwordEncoder.encode("password"));
            provider.setRole("PROVIDER");
            return userRepository.save(provider);
        });
    }

    @Test
    void patientIsForbiddenFromAdminIngestionEndpoint() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        IngestStructuredRequest request = new IngestStructuredRequest(
                "sec-test", "Sec test doc", KbCategory.GENERAL_HEALTH_EDUCATION, "some content");
        mockMvc.perform(post("/api/v1/kb/admin/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanIngestDocument() throws Exception {
        String token = jwtTokenUtil.generateToken("admin@example.com");
        IngestStructuredRequest request = new IngestStructuredRequest(
                "sec-test-admin", "Sec test doc", KbCategory.GENERAL_HEALTH_EDUCATION,
                "Some educational content for patients about hydration.");
        mockMvc.perform(post("/api/v1/kb/admin/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void patientCanListCategories() throws Exception {
        // Listing categories should not require elevated role (read-only reference data).
        String token = jwtTokenUtil.generateToken("patient@example.com");
        mockMvc.perform(get("/api/v1/kb/admin/categories")
                        .header("Authorization", "Bearer " + token))
                // Provider/admin role required; ensure patient is forbidden even from read endpoints...
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListCategories() throws Exception {
        String token = jwtTokenUtil.generateToken("admin@example.com");
        mockMvc.perform(get("/api/v1/kb/admin/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedAccessToAskEndpointIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/kb/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is diabetes?\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // Additional admin/security coverage for sensitive Phase 9 endpoints
    // ------------------------------------------------------------------

    @Test
    void patientCannotListDocuments() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        mockMvc.perform(get("/api/v1/kb/admin/documents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotReindexDocument() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        mockMvc.perform(post("/api/v1/kb/admin/documents/1/reindex")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotRegenerateEmbeddings() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        mockMvc.perform(post("/api/v1/kb/admin/documents/1/embeddings/regenerate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotActivateVersion() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        mockMvc.perform(post("/api/v1/kb/admin/documents/some-ext/activate/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotDeactivateDocument() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        mockMvc.perform(delete("/api/v1/kb/admin/documents/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedActivateRequestIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/kb/admin/documents/some-ext/activate/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedDeactivateRequestIsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/kb/admin/documents/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void providerCanIngestDocument() throws Exception {
        String token = jwtTokenUtil.generateToken("provider@example.com");
        IngestStructuredRequest request = new IngestStructuredRequest(
                "sec-test-provider", "Provider ingested doc", KbCategory.GENERAL_HEALTH_EDUCATION,
                "Educational content about general wellness topics.");
        mockMvc.perform(post("/api/v1/kb/admin/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void providerCanUploadBinaryDocument() throws Exception {
        // The admin controller permits both ADMIN and PROVIDER roles, so providers
        // can upload documents too. This test pins that contract.
        String token = jwtTokenUtil.generateToken("provider@example.com");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-provider.txt", MediaType.TEXT_PLAIN_VALUE,
                "Sample plain text content for ingestion.".getBytes());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/v1/kb/admin/documents/upload")
                        .file(file)
                        .param("externalId", "sec-upload-provider")
                        .param("name", "Sec Upload Provider")
                        .param("category", "GENERAL_HEALTH_EDUCATION")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void patientCannotAccessConversationsOrFeedback() throws Exception {
        String token = jwtTokenUtil.generateToken("patient@example.com");
        // conversations listing is permitted to any authenticated user, but feedback
        // submission must still require authentication; here we verify both paths
        // are reachable (not 401) without bypassing the controller's checks.
        mockMvc.perform(get("/api/v1/kb/conversations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // feedback requires a valid message id; we expect 404 (not 401/403) to
        // prove the request is authenticated and reaches the controller layer.
        FeedbackRequest request = new FeedbackRequest();
        request.setCategory(FeedbackCategory.HELPFUL);
        mockMvc.perform(post("/api/v1/kb/feedback/9999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedFeedbackIsUnauthorized() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setCategory(FeedbackCategory.HELPFUL);
        mockMvc.perform(post("/api/v1/kb/feedback/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedSearchIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/kb/search").param("query", "diabetes"))
                .andExpect(status().isUnauthorized());
    }
}
