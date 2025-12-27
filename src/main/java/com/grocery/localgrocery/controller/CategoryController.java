package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Category;
import com.grocery.localgrocery.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // List categories
    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // Add a category
    @PostMapping
    public Category create(@RequestBody Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        String name = category.getName().trim();

        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        return categoryRepository.save(new Category(name));
    }
}
