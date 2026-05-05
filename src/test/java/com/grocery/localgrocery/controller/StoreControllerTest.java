package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Store;
import com.grocery.localgrocery.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreRepository storeRepository;

    @Test
    void getAllShouldReturnStores() throws Exception {
        Store s1 = new Store("Fresh Mart", "Kathmandu");
        Store s2 = new Store("SuperMart", "Patan");

        when(storeRepository.findAll()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/stores"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].name").value("Fresh Mart"));
    }

    @Test
    void createShouldSaveStore() throws Exception {
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
            {"name":"New Store","location":"Bhaktapur"}
            """;

        mockMvc.perform(post("/api/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("New Store"))
               .andExpect(jsonPath("$.location").value("Bhaktapur"));
    }

    @Test
    void createShouldRejectEmptyName() throws Exception {
        String json = """
            {"name":""}
            """;

        mockMvc.perform(post("/api/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }
}
