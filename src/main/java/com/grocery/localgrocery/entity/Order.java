package com.grocery.localgrocery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who placed the order
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String status; // PLACED, CONFIRMED, DELIVERED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Money breakdown
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // Promo + payment
    private String promoCode;

    @Column(nullable = false)
    private String paymentMethod; // COD or CARD

    // Store only safe card summary (never store CVV)
    private String cardBrand;
    private String cardLast4;
    private String cardExpiry; // MM/YY

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = "PLACED";
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.paymentMethod = "COD";
    }

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
}
