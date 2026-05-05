package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void defaultConstructorShouldInitializeDefaults() {
        Order order = new Order();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo("PLACED");
        assertThat(order.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getTax()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getShippingFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getPaymentMethod()).isEqualTo("COD");
        assertThat(order.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(order.getCodCollected()).isFalse();
    }

    @Test
    void shouldSetAndGetFields() {
        Order order = new Order();
        User user = new User();
        user.setEmail("user@test.com");
        order.setUser(user);

        order.setDeliveryAddress("Kathmandu, Nepal");
        order.setStatus("DELIVERED");
        order.setSubtotal(new BigDecimal("100.00"));
        order.setTax(new BigDecimal("13.00"));
        order.setShippingFee(new BigDecimal("50.00"));
        order.setTotal(new BigDecimal("163.00"));
        order.setPromoCode("SAVE10");
        order.setPaymentMethod("CARD");
        order.setCardBrand("visa");
        order.setCardLast4("4242");
        order.setCardExpiry("12/26");
        order.setDeliveryNotes("Leave at front door");
        order.setPaymentStatus("COMPLETED");
        order.setCodCollected(true);

        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getDeliveryAddress()).isEqualTo("Kathmandu, Nepal");
        assertThat(order.getStatus()).isEqualTo("DELIVERED");
        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(order.getTax()).isEqualByComparingTo(new BigDecimal("13.00"));
        assertThat(order.getShippingFee()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("163.00"));
        assertThat(order.getPromoCode()).isEqualTo("SAVE10");
        assertThat(order.getPaymentMethod()).isEqualTo("CARD");
        assertThat(order.getCardBrand()).isEqualTo("visa");
        assertThat(order.getCardLast4()).isEqualTo("4242");
        assertThat(order.getCardExpiry()).isEqualTo("12/26");
        assertThat(order.getDeliveryNotes()).isEqualTo("Leave at front door");
        assertThat(order.getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(order.getCodCollected()).isTrue();
    }

    @Test
    void shouldSetDeliveryPersonAndTimestamps() {
        Order order = new Order();
        User deliveryPerson = new User();
        deliveryPerson.setEmail("delivery@test.com");
        order.setDeliveryPerson(deliveryPerson);

        LocalDateTime now = LocalDateTime.now();
        order.setAssignedAt(now);
        order.setPickedUpAt(now.plusMinutes(30));
        order.setDeliveredAt(now.plusHours(2));

        assertThat(order.getDeliveryPerson()).isEqualTo(deliveryPerson);
        assertThat(order.getAssignedAt()).isEqualTo(now);
        assertThat(order.getPickedUpAt()).isEqualTo(now.plusMinutes(30));
        assertThat(order.getDeliveredAt()).isEqualTo(now.plusHours(2));
    }
}
