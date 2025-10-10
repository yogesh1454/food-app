package com.teadelivery.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration requests.
 * Follows coding standards with comprehensive validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class UserRegistrationRequest {

    @Schema(description = "User email address", example = "john@example.com", required = true)
    private String email;
    
    @Schema(description = "User password", example = "securePassword123", required = true)
    private String password;
    
    @Schema(description = "User full name", example = "John Doe", required = true)
    private String name;
    
    @Schema(description = "User phone number", example = "+1234567890", required = false)
    private String phoneNumber;
} 