package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    void shouldSetAndGetFields() {
        Category category = new Category();
        category.setName("Vegetables");

        assertThat(category.getName()).isEqualTo("Vegetables");
    }

    @Test
    void parameterizedConstructorShouldWork() {
        Category category = new Category("Fruits");
        assertThat(category.getName()).isEqualTo("Fruits");
    }
}
