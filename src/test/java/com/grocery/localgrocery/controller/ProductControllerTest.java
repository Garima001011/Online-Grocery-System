package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Category;
import com.grocery.localgrocery.entity.Product;
import com.grocery.localgrocery.entity.Store;
import com.grocery.localgrocery.repository.CategoryRepository;
import com.grocery.localgrocery.repository.ProductRepository;
import com.grocery.localgrocery.repository.StoreRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private StoreRepository storeRepository;

    @Test
    void getAllShouldReturnProducts() throws Exception {
        Product p1 = new Product();
        p1.setName("Apple");
        p1.setPrice(1.5);

        Product p2 = new Product();
        p2.setName("Banana");
        p2.setPrice(0.5);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].name").value("Apple"));
    }

    @Test
    void getByIdShouldReturnProduct() throws Exception {
        Product product = new Product();
        product.setName("Milk");
        product.setPrice(2.5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Milk"));
    }

    @Test
    void getByIdShouldReturnNotFound() throws Exception {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/99"))
               .andExpect(status().isNotFound());
    }

    @Test
    void createShouldSaveProduct() throws Exception {
        Category category = new Category("Fruits");
        Store store = new Store("Fresh Mart", "KTM");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
            {"name":"Rice","price":50.0,"stock":100,"categoryId":1,"storeId":1,"weightKg":5.0}
            """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Rice"));
    }

    @Test
    void createShouldRejectMissingName() throws Exception {
        String json = """
            {"price":10.0,"stock":5,"categoryId":1,"storeId":1}
            """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void updateShouldModifyProduct() throws Exception {
        Product existing = new Product();
        existing.setName("Old Name");
        existing.setPrice(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        String json = """
            {"name":"New Name","price":15.0,"stock":20}
            """;

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("New Name"));
    }
}
