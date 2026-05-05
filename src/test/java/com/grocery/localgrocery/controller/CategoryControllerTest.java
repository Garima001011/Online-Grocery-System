package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Category;
import com.grocery.localgrocery.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @Test
    void getAllShouldReturnCategories() throws Exception {
        Category c1 = new Category("Fruits");
        Category c2 = new Category("Vegetables");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].name").value("Fruits"));
    }

    @Test
    void createShouldSaveCategory() throws Exception {
        when(categoryRepository.findByNameIgnoreCase("Snacks")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
            {"name":"Snacks"}
            """;

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Snacks"));
    }

    @Test
    void createShouldRejectDuplicate() throws Exception {
        Category existing = new Category("Beverages");
        when(categoryRepository.findByNameIgnoreCase("Beverages")).thenReturn(Optional.of(existing));

        String json = """
            {"name":"Beverages"}
            """;

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isConflict());
    }

    @Test
    void createShouldRejectEmptyName() throws Exception {
        String json = """
            {"name":""}
            """;

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }
}
