package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.User;
import com.grocery.localgrocery.repository.UserRepository;
import com.grocery.localgrocery.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Updated constructor to include EmailService
    public AuthController(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Register request DTO
    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public String role;
        public String phone;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest req) {
        if (req.email == null || req.password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password required");
        }
        if (req.phone == null || req.phone.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        if (userRepository.findByEmail(req.email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // Password cannot be same as email
        if (req.password.equalsIgnoreCase(req.email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be the same as your email");
        }

        User user = new User();
        user.setEmail(req.email);
        user.setPassword(req.password);
        user.setRole(req.role != null ? req.role : "CUSTOMER");
        user.setPhone(req.phone);
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

    // --------------------- Forgot Password ---------------------
    public static class ForgotPasswordRequest {
        public String email;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        userRepository.findByEmail(req.email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
            } catch (Exception e) {
                // Log the error but don't reveal it to the client
                System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            }
        });

        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
    }

    // --------------------- Reset Password ---------------------
    public static class ResetPasswordRequest {
        public String token;
        public String newPassword;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        User user = userRepository.findByResetToken(req.token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        // Password cannot be same as email
        if (req.newPassword.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be the same as your email");
        }

        // Update password and clear token fields
        user.setPassword(req.newPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}