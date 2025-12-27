package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
