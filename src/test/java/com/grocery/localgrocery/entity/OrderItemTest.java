package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    void shouldSetAndGetFields() {
        OrderItem item = new OrderItem();
        item.setQuantity(5);
        item.setPriceAtPurchase(12.5);
        item.setReturnStatus("REQUESTED");
        item.setReturnReason("Damaged");
        item.setReturnDescription("Box was torn");
        item.setRefundAmount(62.5);

        Order order = new Order();
        item.setOrder(order);

        Product product = new Product();
        product.setName("Rice");
        item.setProduct(product);

        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getPriceAtPurchase()).isEqualTo(12.5);
        assertThat(item.getReturnStatus()).isEqualTo("REQUESTED");
        assertThat(item.getReturnReason()).isEqualTo("Damaged");
        assertThat(item.getReturnDescription()).isEqualTo("Box was torn");
        assertThat(item.getRefundAmount()).isEqualTo(62.5);
        assertThat(item.getOrder()).isEqualTo(order);
        assertThat(item.getProduct()).isEqualTo(product);
    }

    @Test
    void defaultReturnStatusShouldBeNone() {
        OrderItem item = new OrderItem();
        assertThat(item.getReturnStatus()).isEqualTo("NONE");
    }
}
