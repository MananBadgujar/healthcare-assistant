package com.healthcare.ragservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kb_documents")
public class KbDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(length = 20000)
    private String content;
    private int version = 1;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String v) { this.title = v; }
    public String getContent() { return content; } public void setContent(String v) { this.content = v; }
    public int getVersion() { return version; } public void setVersion(int v) { this.version = v; }
}
