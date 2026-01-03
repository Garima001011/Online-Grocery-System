package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.User;
import com.grocery.localgrocery.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register request DTO
    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public String role;
    }

    // Register (for testing)
    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest req) {
        if (req.email == null || req.password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password required");
        }

        if (userRepository.findByEmail(req.email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setEmail(req.email);
        user.setPassword(req.password);
        user.setRole(req.role != null ? req.role : "CUSTOMER");

        // Set name if provided
        if (req.name != null) {
            user.setName(req.name);
        }

        return userRepository.save(user);
    }

    // Login
    @PostMapping("/login")
    public User login(@RequestBody User req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getPassword().equals(req.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return user; // later we will return token/session
    }
}