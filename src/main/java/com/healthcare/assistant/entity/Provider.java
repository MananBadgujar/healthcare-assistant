package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "providers")
@Data
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licenseNumber;

    private String specialty;

    private Boolean available = true;

public Provider() {
        }
}