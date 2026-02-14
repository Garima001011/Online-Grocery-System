package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.user.id = ?1 ORDER BY nl.sentAt DESC")
    List<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId);
}