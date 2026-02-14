package com.grocery.localgrocery.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"password"})
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String status; // PLACED, ASSIGNED, PICKED_UP, DELIVERED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    private String promoCode;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy; // ID of user who cancelled (customer or admin)

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "payment_received_at")
    private LocalDateTime paymentReceivedAt;

    @Column(name = "delivery_proof_image_url")
    private String deliveryProofImageUrl;

    @Column(name = "cod_collected")
    private Boolean codCollected = false;

    private String cardBrand;
    private String cardLast4;
    private String cardExpiry;

    @JsonIgnoreProperties({"password"})
    @ManyToOne
    @JoinColumn(name = "delivery_user_id")
    private User deliveryPerson;

    @Column(length = 500)
    private String deliveryNotes;

    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Column(name = "payment_status")
    private String paymentStatus = "PENDING"; // PENDING, COMPLETED, REFUNDED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"order"})
    private List<OrderItem> items;

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = "PLACED";
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.paymentMethod = "COD";
        this.paymentStatus = "PENDING";
    }

    // Getters and setters for all fields
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public String getPromoCode() { return promoCode; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCardBrand() { return cardBrand; }
    public String getCardLast4() { return cardLast4; }
    public String getCardExpiry() { return cardExpiry; }
    public User getDeliveryPerson() { return deliveryPerson; }
    public String getDeliveryNotes() { return deliveryNotes; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public String getPaymentStatus() { return paymentStatus; }
    public List<OrderItem> getItems() { return items; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public Long getCancelledBy() { return cancelledBy; }
    public String getCancelledReason() { return cancelReason; }
    public Boolean getCodCollected() {return codCollected;}
    public LocalDateTime getPaymentReceivedAt() {return paymentReceivedAt;}
    public String getDeliveryProofImageUrl() { return deliveryProofImageUrl; }

    public void setUser(User user) { this.user = user; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setStatus(String status) { this.status = status; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }
    public void setDeliveryPerson(User deliveryPerson) { this.deliveryPerson = deliveryPerson; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setCancelledBy(Long cancelledBy) { this.cancelledBy = cancelledBy; }
    public void setCancelledReason(String cancelReason) { this.cancelReason = cancelReason; }
    public void setCodCollected(Boolean codCollected) { this.codCollected = codCollected; }
    public void setPaymentReceivedAt(LocalDateTime paymentReceivedAt) { this.paymentReceivedAt = paymentReceivedAt; }
    public void setDeliveryProofImageUrl(String deliveryProofImageUrl) {this.deliveryProofImageUrl = deliveryProofImageUrl;}
}