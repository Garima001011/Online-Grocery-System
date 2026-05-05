package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void shouldSetAndGetFields() {
        Product product = new Product();
        product.setName("Apple");
        product.setPrice(1.99);
        product.setStock(10);
        product.setDescription("Fresh red apple");
        product.setImageUrl("/uploads/apple.png");
        product.setWeightKg(0.5);

        Category category = new Category();
        category.setName("Fruits");
        product.setCategory(category);

        Store store = new Store();
        store.setName("Fresh Mart");
        product.setStore(store);

        assertThat(product.getName()).isEqualTo("Apple");
        assertThat(product.getPrice()).isEqualTo(1.99);
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(product.getDescription()).isEqualTo("Fresh red apple");
        assertThat(product.getImageUrl()).isEqualTo("/uploads/apple.png");
        assertThat(product.getWeightKg()).isEqualTo(0.5);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getStore()).isEqualTo(store);
    }

    @Test
    void defaultConstructorShouldWork() {
        Product product = new Product();
        assertThat(product).isNotNull();
    }
}
