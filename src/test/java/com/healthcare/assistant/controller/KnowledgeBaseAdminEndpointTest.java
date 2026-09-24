package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.healthcare.assistant.entity.KbDocument;
import com.healthcare.assistant.rag.ingestion.DocumentIngestionService;

/**
 * Phase 6 endpoint coverage: KnowledgeBaseAdmin success paths (documents,
 * versions, document, reindex, regenerate, activate, deactivate).
 */
@WebMvcTest(KnowledgeBaseAdminController.class)
@WithMockUser(roles = "ADMIN")
class KnowledgeBaseAdminEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentIngestionService ingestionService;

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class MethodSecurityConfig {
    }

    private KbDocument document() {
        KbDocument document = new KbDocument();
        document.setId(1L);
        document.setExternalId("ext-1");
        document.setName("Doc");
        return document;
    }

    @Test
    void listDocuments_returnsOk() throws Exception {
        when(ingestionService.findAll()).thenReturn(Collections.singletonList(document()));
        mockMvc.perform(get("/api/v1/kb/admin/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("ext-1"));
    }

    @Test
    void listVersions_returnsOk() throws Exception {
        when(ingestionService.findVersions("ext-1"))
                .thenReturn(Collections.singletonList(document()));
        mockMvc.perform(get("/api/v1/kb/admin/documents/ext-1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDocument_returnsOk() throws Exception {
        when(ingestionService.findDocumentById(1L)).thenReturn(document());
        mockMvc.perform(get("/api/v1/kb/admin/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Doc"));
    }

    @Test
    void reindex_returnsOk() throws Exception {
        when(ingestionService.reindex(1L)).thenReturn(document());
        mockMvc.perform(post("/api/v1/kb/admin/documents/1/reindex").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("ext-1"));
    }

    @Test
    void regenerateEmbeddings_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/kb/admin/documents/1/embeddings/regenerate").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateVersion_returnsOk() throws Exception {
        when(ingestionService.activateVersion("ext-1", "v2")).thenReturn(document());
        mockMvc.perform(post("/api/v1/kb/admin/documents/ext-1/activate/v2").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void deactivate_returnsOk() throws Exception {
        when(ingestionService.deactivate(1L)).thenReturn(document());
        mockMvc.perform(delete("/api/v1/kb/admin/documents/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Doc"));
    }

    @Test
    void uploadEmptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/kb/admin/documents/upload")
                .file(empty)
                .param("externalId", "ext-1")
                .param("name", "Doc")
                .param("category", "MEDICATIONS")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadValidFile_returnsCreated() throws Exception {
        when(ingestionService.ingest(any())).thenReturn(document());
        MockMultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain",
                "hello".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/kb/admin/documents/upload")
                .file(file)
                .param("externalId", "ext-1")
                .param("name", "Doc")
                .param("category", "MEDICATIONS")
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void ingestStructured_returnsCreated() throws Exception {
        when(ingestionService.ingest(any())).thenReturn(document());
        mockMvc.perform(post("/api/v1/kb/admin/documents").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"externalId\":\"ext-1\",\"name\":\"Doc\",\"category\":\"MEDICATIONS\","
                        + "\"content\":\"Some content\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void categories_returnsOk() throws Exception {
        when(ingestionService.supportedCategories()).thenReturn(Collections.singletonList("MEDICATIONS"));
        mockMvc.perform(get("/api/v1/kb/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("MEDICATIONS"));
    }

    @Test
    void adminRoleEnforced_onDocuments() throws Exception {
        mockMvc.perform(get("/api/v1/kb/admin/documents")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("plain").roles("USER")))
                .andExpect(status().isForbidden());
    }
}
