package com.grocery.localgrocery.repository;

import com.grocery.localgrocery.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByNameIgnoreCase(String name);
}
