package com.grocery.localgrocery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "notification_type")
    private String type; // OFFER, ALERT, INFO, DEAL

    @Column(name = "target_audience")
    private String targetAudience; // ALL, CUSTOMERS, DELIVERY, SPECIFIC

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    private String status; // DRAFT, SCHEDULED, SENT, CANCELLED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.status = "DRAFT";
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getTargetAudience() { return targetAudience; }
    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public void setScheduledFor(LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}