package com.teadelivery.user.password.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.user.password.dto.PasswordChangeRequest;
import com.teadelivery.user.password.dto.PasswordResetConfirmRequest;
import com.teadelivery.user.password.dto.PasswordResetRequest;
import com.teadelivery.user.password.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PasswordController.
 * Follows coding standards with comprehensive test coverage.
 */
@WebMvcTest(PasswordController.class)
class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordService passwordService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testPassword = "SecurePass123!";
    }

    @Test
    void getPasswordRequirements_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/password/requirements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.min_length").value(8))
                .andExpect(jsonPath("$.require_uppercase").value(true))
                .andExpect(jsonPath("$.require_lowercase").value(true))
                .andExpect(jsonPath("$.require_digit").value(true))
                .andExpect(jsonPath("$.require_special_char").value(true))
                .andExpect(jsonPath("$.max_history").value(5));
    }

    @Test
    void requestPasswordReset_Success() throws Exception {
        // Arrange
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email(testEmail)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset instructions sent to your email");
        response.put("email_sent", true);

        when(passwordService.requestPasswordReset(request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password reset instructions sent to your email"))
                .andExpect(jsonPath("$.email_sent").value(true));
    }

    @Test
    void requestPasswordReset_InvalidEmail() throws Exception {
        // Arrange
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("invalid-email")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPasswordReset_EmptyEmail() throws Exception {
        // Arrange
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmPasswordReset_Success() throws Exception {
        // Arrange
        String resetToken = "ABC123DEF456";
        PasswordResetConfirmRequest request = PasswordResetConfirmRequest.builder()
                .token(resetToken)
                .newPassword(testPassword)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        response.put("password_reset", true);

        when(passwordService.confirmPasswordReset(request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password reset successfully"))
                .andExpect(jsonPath("$.password_reset").value(true));
    }

    @Test
    void confirmPasswordReset_InvalidPassword() throws Exception {
        // Arrange
        String resetToken = "ABC123DEF456";
        PasswordResetConfirmRequest request = PasswordResetConfirmRequest.builder()
                .token(resetToken)
                .newPassword("weak")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmPasswordReset_EmptyToken() throws Exception {
        // Arrange
        PasswordResetConfirmRequest request = PasswordResetConfirmRequest.builder()
                .token("")
                .newPassword(testPassword)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void changePassword_Success() throws Exception {
        // Arrange
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("oldPassword123!")
                .newPassword(testPassword)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        response.put("password_changed", true);

        when(passwordService.changePassword(eq(UUID.fromString("test-user-id")), any(PasswordChangeRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password changed successfully"))
                .andExpect(jsonPath("$.password_changed").value(true));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void changePassword_InvalidCurrentPassword() throws Exception {
        // Arrange
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("")
                .newPassword(testPassword)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void changePassword_WeakNewPassword() throws Exception {
        // Arrange
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("oldPassword123!")
                .newPassword("weak")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_Unauthorized() throws Exception {
        // Arrange
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("oldPassword123!")
                .newPassword(testPassword)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
} 