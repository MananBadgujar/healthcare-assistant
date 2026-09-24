package com.healthcare.appointmentservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private Long providerId;
    private String startTime;
    private String status = "PENDING";
    @Column(unique = true)
    private String idempotencyKey;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; } public void setPatientId(Long v) { this.patientId = v; }
    public Long getProviderId() { return providerId; } public void setProviderId(Long v) { this.providerId = v; }
    public String getStartTime() { return startTime; } public void setStartTime(String v) { this.startTime = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
    public String getIdempotencyKey() { return idempotencyKey; } public void setIdempotencyKey(String v) { this.idempotencyKey = v; }
}
