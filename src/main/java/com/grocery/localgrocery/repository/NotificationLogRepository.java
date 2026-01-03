package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}