package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "records")
@Data
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String type;

    private String content;

    private LocalDateTime createdAt;

    public Record(Long patientId, String type, String content, String createdAt) {
        this.type = type;
        this.content = content;
        this.createdAt = LocalDateTime.parse(createdAt);
    }

    /** Required by JPA */
    public Record() { }
}