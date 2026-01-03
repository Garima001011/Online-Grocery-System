package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByReturnStatusNot(String status); // Add this method

    long countByReturnStatus(String status);

    @Query("SELECT oi.id, o.id, u.name, u.email, p.name, oi.quantity, oi.priceAtPurchase, " +
            "oi.returnStatus, oi.returnReason, oi.returnRequestedAt, oi.refundAmount " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN o.user u " +
            "JOIN oi.product p " +
            "WHERE oi.returnStatus != 'NONE' " +
            "ORDER BY oi.returnRequestedAt DESC")
    List<Object[]> findReturnsForAdmin();
}