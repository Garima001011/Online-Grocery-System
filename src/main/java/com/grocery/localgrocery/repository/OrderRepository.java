package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
