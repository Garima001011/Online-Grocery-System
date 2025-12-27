package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.Store;
import com.grocery.localgrocery.repository.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreRepository storeRepository;

    public StoreController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public List<Store> getAll() {
        return storeRepository.findAll();
    }

    public static class CreateStoreRequest {
        public String name;
        public String location;
    }

    @PostMapping
    public Store create(@RequestBody CreateStoreRequest req) {
        if (req.name == null || req.name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Store name is required");
        }

        Store s = new Store();
        s.setName(req.name.trim());
        s.setLocation(req.location == null ? "" : req.location.trim());

        try {
            return storeRepository.save(s);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Store name already exists");
        }
    }
}
