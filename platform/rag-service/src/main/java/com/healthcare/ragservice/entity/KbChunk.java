package com.healthcare.ragservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kb_chunks")
public class KbChunk {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long documentId;
    @Column(length = 5000)
    private String text;
    @Column(length = 20000)
    private String embedding;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getDocumentId() { return documentId; } public void setDocumentId(Long v) { this.documentId = v; }
    public String getText() { return text; } public void setText(String v) { this.text = v; }
    public String getEmbedding() { return embedding; } public void setEmbedding(String v) { this.embedding = v; }
}
