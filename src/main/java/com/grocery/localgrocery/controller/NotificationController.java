package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.NotificationLog;
import com.grocery.localgrocery.repository.NotificationLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationController(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    // Customer/Delivery fetch their notifications
    @GetMapping("/notifications/my")
    public List<Map<String, Object>> myNotifications(@RequestParam Long userId) {
        List<NotificationLog> logs = notificationLogRepository.findByUserIdOrderBySentAtDesc(userId);

        // Return notification + sentAt in a simple JSON
        return logs.stream().map(nl -> Map.of(
                "id", nl.getId(),
                "sentAt", nl.getSentAt(),
                "notification", Map.of(
                        "id", nl.getNotification().getId(),
                        "title", nl.getNotification().getTitle(),
                        "message", nl.getNotification().getMessage(),
                        "type", nl.getNotification().getType(),
                        "targetAudience", nl.getNotification().getTargetAudience(),
                        "createdAt", nl.getNotification().getCreatedAt()
                )
        )).collect(Collectors.toList());
    }
}