package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.dto.KbArticleCreateRequest;
import com.healthcare.assistant.entity.enums.KbCategory;
import com.healthcare.assistant.entity.enums.KbDocumentFormat;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Request to ingest a document into the knowledge base. The request can be used
 * in two ways:
 * <ul>
 *   <li>Upload raw bytes (+ {@code format}) — used by the admin upload endpoint
 *       for PDF/TXT; the pipeline extracts text from {@code payload}.</li>
 *   <li>Pass {@code explicitText} directly — used by the structured-content API
 *       where the caller posts the text inline; this is also the path used by
 *       the legacy {@link KbArticleCreateRequest} adapter.</li>
 * </ul>
 * The {@code payload} and {@code explicitText} fields are mutually sufficient:
 * at least one of them must supply non-empty content validated by the
 * ingestion service.
 */
public class IngestDocumentRequest {

    private String externalId;
    private String name;
    private String source;
    private KbCategory category;
    private String topic;
    private String version;
    private KbDocumentFormat format;
    private byte[] payload;
    private String explicitText;
    private LocalDate publicationDate;
    private LocalDate updateDate;

    public IngestDocumentRequest() {
    }

    public IngestDocumentRequest(String externalId, String name, KbCategory category,
                                 KbDocumentFormat format, String explicitText) {
        this.externalId = externalId;
        this.name = name;
        this.category = category;
        this.format = format;
        this.explicitText = explicitText;
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

    public KbDocumentFormat getFormat() { return format; }
    public void setFormat(KbDocumentFormat format) { this.format = format; }

    public byte[] getPayload() { return payload; }
    public void setPayload(byte[] payload) { this.payload = payload; }

    public String getExplicitText() { return explicitText; }
    public void setExplicitText(String explicitText) { this.explicitText = explicitText; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public LocalDate getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDate updateDate) { this.updateDate = updateDate; }

    public IngestDocumentRequest apply(Consumer<IngestDocumentRequest> consumer) {
        consumer.accept(this);
        return this;
    }
}
