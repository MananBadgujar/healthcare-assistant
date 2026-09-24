package com.healthcare.assistant.rag.dto;

import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request body for {@code POST /api/v1/kb/admin/documents} (structured content
 * ingestion). Text content is supplied inline rather than as an uploaded file.
 */
public class IngestStructuredRequest {

    @NotBlank
    private String externalId;
    @NotBlank
    private String name;
    private String source;
    @NotNull
    private KbCategory category;
    private String topic;
    private String version;
    @NotBlank
    private String content;
    private LocalDate publicationDate;
    private LocalDate updateDate;

    public IngestStructuredRequest() {
    }

    public IngestStructuredRequest(String externalId, String name, KbCategory category, String content) {
        this.externalId = externalId;
        this.name = name;
        this.category = category;
        this.content = content;
        this.version = "1";
    }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public KbCategory getCategory() { return category; }
    public void setCategory(KbCategory category) { this.category = category; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public LocalDate getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDate updateDate) { this.updateDate = updateDate; }

    public KbDocumentFormat getFormat() {
        return KbDocumentFormat.STRUCTURED;
    }
}
