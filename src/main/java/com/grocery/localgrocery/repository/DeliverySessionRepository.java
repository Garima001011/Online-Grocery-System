package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.DeliverySession;
import com.grocery.localgrocery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliverySessionRepository extends JpaRepository<DeliverySession, Long> {
    List<DeliverySession> findByDeliveryPersonAndEndTimeIsNull(User deliveryPerson);
    List<DeliverySession> findByDeliveryPersonAndStartTimeBetween(User deliveryPerson, LocalDateTime start, LocalDateTime end);
}