package com.healthcare.providerservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "providers")
public class Provider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String specialty;
    private String facility;
    @Column(length = 2000)
    private String availableSlots;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getSpecialty() { return specialty; } public void setSpecialty(String v) { this.specialty = v; }
    public String getFacility() { return facility; } public void setFacility(String v) { this.facility = v; }
    public String getAvailableSlots() { return availableSlots; } public void setAvailableSlots(String v) { this.availableSlots = v; }
}
