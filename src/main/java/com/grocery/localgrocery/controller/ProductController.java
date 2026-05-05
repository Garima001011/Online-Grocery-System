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

    // Get single product by ID (ADD THIS IF MISSING)
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public static class CreateProductRequest {
        public String name;
        public double price;
        public int stock;
        public Long categoryId;
        public Long storeId;
        public double weightKg;  // ✅ ADD THIS FIELD
        public String description;  // ✅ Optional: add description
        public String imageUrl;     // ✅ Optional: add image URL
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
        p.setWeightKg(req.weightKg);  // ✅ ADD THIS LINE
        p.setCategory(category);
        p.setStore(store);

        // Optional fields
        if (req.description != null) {
            p.setDescription(req.description);
        }
        if (req.imageUrl != null) {
            p.setImageUrl(req.imageUrl);
        }

        return productRepository.save(p);
    }

    // ✅ ADD UPDATE endpoint for editing products (including weight)
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody CreateProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (req.name != null && !req.name.trim().isEmpty()) {
            p.setName(req.name.trim());
        }
        if (req.price >= 0) {
            p.setPrice(req.price);
        }
        if (req.stock >= 0) {
            p.setStock(req.stock);
        }
        if (req.weightKg > 0) {
            p.setWeightKg(req.weightKg);  // ✅ ADD WEIGHT UPDATE
        }
        if (req.description != null) {
            p.setDescription(req.description);
        }
        if (req.imageUrl != null) {
            p.setImageUrl(req.imageUrl);
        }
        if (req.categoryId != null) {
            Category category = categoryRepository.findById(req.categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            p.setCategory(category);
        }
        if (req.storeId != null) {
            Store store = storeRepository.findById(req.storeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
            p.setStore(store);
        }

        return productRepository.save(p);
    }
}