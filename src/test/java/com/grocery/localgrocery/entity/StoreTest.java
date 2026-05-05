package com.grocery.localgrocery.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StoreTest {

    @Test
    void shouldSetAndGetFields() {
        Store store = new Store();
        store.setName("Fresh Mart");
        store.setLocation("Patan");

        assertThat(store.getName()).isEqualTo("Fresh Mart");
        assertThat(store.getLocation()).isEqualTo("Patan");
    }

    @Test
    void parameterizedConstructorShouldWork() {
        Store store = new Store("SuperMart", "Kathmandu");
        assertThat(store.getName()).isEqualTo("SuperMart");
        assertThat(store.getLocation()).isEqualTo("Kathmandu");
    }
}
