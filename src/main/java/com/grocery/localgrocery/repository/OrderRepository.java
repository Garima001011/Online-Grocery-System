package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByDeliveryPersonId(Long deliveryPersonId);
    List<Order> findByUserId(Long userId);

    @Query("SELECT SUM(o.total) FROM Order o")
    BigDecimal calculateTotalRevenue();

    long countByStatus(String status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Fixed: Use native query for limit
    @Query(value = "SELECT * FROM orders ORDER BY created_at DESC LIMIT ?1", nativeQuery = true)
    List<Order> findTopNByOrderByCreatedAtDesc(int limit);
}