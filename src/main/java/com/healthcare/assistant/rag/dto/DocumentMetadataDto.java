package com.healthcare.assistant.rag.dto;

import com.healthcare.assistant.entity.KbDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lightweight view of a stored {@link KbDocument} for the admin list /
 * metadata endpoints. Excludes the document content (which can be large) to
 * keep responses compact.
 */
public class DocumentMetadataDto {

    private final Long id;
    private final String externalId;
    private final String name;
    private final String source;
    private final String category;
    private final String topic;
    private final String version;
    private final String format;
    private final LocalDate publicationDate;
    private final LocalDate updateDate;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public DocumentMetadataDto(KbDocument document) {
        this.id = document.getId();
        this.externalId = document.getExternalId();
        this.name = document.getName();
        this.source = document.getSource();
        this.category = document.getCategory() == null ? null : document.getCategory().name();
        this.topic = document.getTopic();
        this.version = document.getVersion();
        this.format = document.getFormat() == null ? null : document.getFormat().name();
        this.publicationDate = document.getPublicationDate();
        this.updateDate = document.getUpdateDate();
        this.active = document.isActive();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getSource() { return source; }
    public String getCategory() { return category; }
    public String getTopic() { return topic; }
    public String getVersion() { return version; }
    public String getFormat() { return format; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public LocalDate getUpdateDate() { return updateDate; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
