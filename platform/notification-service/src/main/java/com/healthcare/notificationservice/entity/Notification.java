package com.healthcare.notificationservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String eventId;
    private String type;
    @Column(length = 4000)
    private String message;
    private String recipient;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEventId() { return eventId; } public void setEventId(String v) { this.eventId = v; }
    public String getType() { return type; } public void setType(String v) { this.type = v; }
    public String getMessage() { return message; } public void setMessage(String v) { this.message = v; }
    public String getRecipient() { return recipient; } public void setRecipient(String v) { this.recipient = v; }
}
