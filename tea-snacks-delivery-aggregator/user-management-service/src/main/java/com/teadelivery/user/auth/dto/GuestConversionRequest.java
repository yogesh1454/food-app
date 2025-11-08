package com.teadelivery.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for guest to user conversion requests.
 * Follows coding standards with comprehensive conversion data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Guest to user conversion request")
public class GuestConversionRequest {

    @Schema(description = "Guest user ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private String guestUserId;
    
    @Schema(description = "User email address", example = "john@example.com", required = false)
    private String email;
    
    @Schema(description = "User password", example = "securePassword123", required = true)
    private String password;
    
    @Schema(description = "User full name", example = "John Doe", required = true)
    private String name;
    
    @Schema(description = "User phone number", example = "+1234567890", required = false)
    private String phoneNumber;
} 