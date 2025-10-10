package com.teadelivery.user.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProfileController.
 * Follows coding standards with comprehensive test coverage.
 */
@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private ProfileResponse testProfileResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testProfileResponse = ProfileResponse.builder()
                .userId(testUserId)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .profileCompletionPercentage(75)
                .build();
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getUserProfile_Success() throws Exception {
        // Arrange
        when(profileService.getUserProfile(eq(UUID.fromString("test-user-id"))))
                .thenReturn(testProfileResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_id").value(testUserId.toString()))
                .andExpect(jsonPath("$.first_name").value("Test"))
                .andExpect(jsonPath("$.last_name").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phone_number").value("+1234567890"))
                .andExpect(jsonPath("$.profile_completion_percentage").value(75));
    }

    @Test
    void getUserProfile_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateUserProfile_Success() throws Exception {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        ProfileResponse updatedResponse = ProfileResponse.builder()
                .userId(testUserId)
                .firstName("Updated")
                .lastName("Name")
                .email("test@example.com")
                .profileCompletionPercentage(80)
                .build();

        when(profileService.updateUserProfile(eq(UUID.fromString("test-user-id")), any(ProfileUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.first_name").value("Updated"))
                .andExpect(jsonPath("$.last_name").value("Name"))
                .andExpect(jsonPath("$.profile_completion_percentage").value(80));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateUserProfile_InvalidRequest() throws Exception {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("") // Invalid empty first name
                .lastName("Name")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserProfile_Unauthorized() throws Exception {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void uploadProfilePicture_Success() throws Exception {
        // Arrange
        String expectedUrl = "https://example.com/profile.jpg";
        when(profileService.uploadProfilePicture(eq(UUID.fromString("test-user-id")), any()))
                .thenReturn(expectedUrl);

        // Act & Assert
        mockMvc.perform(multipart("/api/users/profile/picture")
                        .file("file", "test image content".getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profile_picture_url").value(expectedUrl));
    }

    @Test
    void uploadProfilePicture_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/users/profile/picture")
                        .file("file", "test image content".getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void requestEmailVerification_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/profile/verify-email")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Email verification request sent"));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void requestPhoneVerification_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/profile/verify-phone")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Phone verification request sent"));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void confirmEmailVerification_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/profile/verify-email/confirm")
                        .param("token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void confirmPhoneVerification_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/profile/verify-phone/confirm")
                        .param("otp", "123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Phone verified successfully"));
    }
} 