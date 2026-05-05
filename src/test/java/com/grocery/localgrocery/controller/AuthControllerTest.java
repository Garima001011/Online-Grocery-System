package com.grocery.localgrocery.controller;

import com.grocery.localgrocery.entity.User;
import com.grocery.localgrocery.repository.UserRepository;
import com.grocery.localgrocery.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @Test
    void registerShouldCreateUser() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
            {"name":"Test User","email":"test@example.com","password":"secret123","phone":"1234567890"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("test@example.com"))
               .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void registerShouldRejectMissingEmail() throws Exception {
        String json = """
            {"password":"secret","phone":"1234567890"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {
        User existing = new User();
        existing.setEmail("dup@example.com");
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(existing));

        String json = """
            {"email":"dup@example.com","password":"secret","phone":"1234567890"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isConflict());
    }

    @Test
    void registerShouldRejectPasswordSameAsEmail() throws Exception {
        String json = """
            {"email":"same@example.com","password":"same@example.com","phone":"1234567890"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturnUser() throws Exception {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("pass123");
        user.setName("Alice");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        String json = """
            {"email":"a@b.com","password":"pass123"}
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("a@b.com"))
               .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void loginShouldRejectInvalidCredentials() throws Exception {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("correct");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        String json = """
            {"email":"a@b.com","password":"wrong"}
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPasswordShouldReturnSuccess() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        String json = """
            {"email":"user@test.com"}
            """;

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resetPasswordShouldUpdatePassword() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");
        user.setResetToken(UUID.randomUUID().toString());
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        when(userRepository.findByResetToken(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        String json = """
            {"token":"valid-token","newPassword":"newSecret123"}
            """;

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }
}
