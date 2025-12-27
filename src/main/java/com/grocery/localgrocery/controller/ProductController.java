package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Category;
import com.grocery.localgrocery.entity.Product;
import com.grocery.localgrocery.entity.Store;
import com.grocery.localgrocery.repository.CategoryRepository;
import com.grocery.localgrocery.repository.ProductRepository;
import com.grocery.localgrocery.repository.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public ProductController(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
    }

    // Single GET with optional filters
    @GetMapping
    public List<Product> getAll(@RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) Long storeId,
                                @RequestParam(required = false) String q) {

        List<Product> all = productRepository.findAll();

        if (categoryId != null) {
            all = all.stream()
                    .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                    .toList();
        }
        if (storeId != null) {
            all = all.stream()
                    .filter(p -> p.getStore() != null && storeId.equals(p.getStore().getId()))
                    .toList();
        }
        if (q != null && !q.trim().isEmpty()) {
            String s = q.trim().toLowerCase();
            all = all.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(s))
                    .toList();
        }
        return all;
    }

    public static class CreateProductRequest {
        public String name;
        public double price;
        public int stock;
        public Long categoryId;
        public Long storeId;
    }

    @PostMapping
    public Product create(@RequestBody CreateProductRequest req) {
        if (req.name == null || req.name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (req.price < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative");
        }
        if (req.stock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock cannot be negative");
        }
        if (req.categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required");
        }
        if (req.storeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storeId is required");
        }

        Store store = storeRepository.findById(req.storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        Category category = categoryRepository.findById(req.categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Product p = new Product();
        p.setName(req.name.trim());
        p.setPrice(req.price);
        p.setStock(req.stock);
        p.setCategory(category);
        p.setStore(store); // IMPORTANT

        return productRepository.save(p);
    }
}
