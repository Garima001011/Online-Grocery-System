package com.grocery.localgrocery.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"user", "deliveryAddress", "status", "createdAt"})
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double priceAtPurchase;

    // Return/Refund fields
    @Column(name = "return_status")
    private String returnStatus = "NONE"; // NONE, REQUESTED, APPROVED, REJECTED, REFUNDED

    @Column(name = "return_reason")
    private String returnReason;

    @Column(name = "return_description", length = 1000)
    private String returnDescription;

    @Column(name = "return_requested_at")
    private LocalDateTime returnRequestedAt;

    @Column(name = "return_processed_at")
    private LocalDateTime returnProcessedAt;

    @Column(name = "refund_amount")
    private Double refundAmount;

    public OrderItem() {}

    // Getters and setters for all fields
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getPriceAtPurchase() { return priceAtPurchase; }
    public String getReturnStatus() { return returnStatus; }
    public String getReturnReason() { return returnReason; }
    public String getReturnDescription() { return returnDescription; }
    public LocalDateTime getReturnRequestedAt() { return returnRequestedAt; }
    public LocalDateTime getReturnProcessedAt() { return returnProcessedAt; }
    public Double getRefundAmount() { return refundAmount; }

    public void setOrder(Order order) { this.order = order; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public void setReturnDescription(String returnDescription) { this.returnDescription = returnDescription; }
    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) { this.returnRequestedAt = returnRequestedAt; }
    public void setReturnProcessedAt(LocalDateTime returnProcessedAt) { this.returnProcessedAt = returnProcessedAt; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }
}