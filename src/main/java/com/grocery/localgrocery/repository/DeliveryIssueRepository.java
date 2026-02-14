package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.DeliveryIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryIssueRepository extends JpaRepository<DeliveryIssue, Long> {
    List<DeliveryIssue> findByOrderId(Long orderId);
    List<DeliveryIssue> findByReportedById(Long deliveryPersonId);
}