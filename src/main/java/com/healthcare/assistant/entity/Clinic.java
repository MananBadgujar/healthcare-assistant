package com.healthcare.assistant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "clinics")
@Data
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
     private String name;
    private String street;
    private String phone;
    private String email;
    private String address;
    private String description;

    public Clinic() {}

    public Clinic(String name, String street, String phone, String email, String description) {
        this.name = name;
        this.street = street;
        this.phone = phone;
        this.email = email;
        this.description = description;
    }
}