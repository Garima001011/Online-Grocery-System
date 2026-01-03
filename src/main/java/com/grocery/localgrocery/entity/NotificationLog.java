package com.grocery.localgrocery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Getters and setters
    public Long getId() { return id; }
    public Notification getNotification() { return notification; }
    public User getUser() { return user; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getReadAt() { return readAt; }

    public void setNotification(Notification notification) { this.notification = notification; }
    public void setUser(User user) { this.user = user; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}