package com.teadelivery.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration responses.
 * Follows coding standards with comprehensive response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration response")
public class UserRegistrationResponse {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;
    
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "Token expiration time in seconds", example = "3600")
    private Long expiresIn;
    
    @Schema(description = "Username (email or phone)", example = "john@example.com")
    private String username;
    
    @Schema(description = "User role", example = "CUSTOMER")
    private String role;
    
    @Schema(description = "User type", example = "REGISTERED")
    private String userType;
    
    @Schema(description = "Profile completion percentage", example = "25")
    private Integer profileCompletion;
    
    @Schema(description = "Response message", example = "User registered successfully")
    private String message;
} 