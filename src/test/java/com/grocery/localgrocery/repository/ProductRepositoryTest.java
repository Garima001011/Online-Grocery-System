package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProduct() {
        Product product = new Product();
        product.setName("Milk");
        product.setPrice(2.5);
        product.setStock(5);

        Product saved = productRepository.save(product);

        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Milk");
        assertThat(found.get().getPrice()).isEqualTo(2.5);
    }

    @Test
    void shouldFindAllProducts() {
        Product p1 = new Product();
        p1.setName("Apple");
        p1.setPrice(1.0);

        Product p2 = new Product();
        p2.setName("Banana");
        p2.setPrice(0.5);

        productRepository.save(p1);
        productRepository.save(p2);

        assertThat(productRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldDeleteProduct() {
        Product product = new Product();
        product.setName("Orange");
        product.setPrice(1.2);

        Product saved = productRepository.save(product);
        productRepository.deleteById(saved.getId());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }
}
