package com.teadelivery.user.registration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration response data")
public class RegistrationResponse {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's name", example = "John Doe")
    private String name;

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "User type", example = "REGISTERED")
    private String userType;

    @Schema(description = "User role", example = "CUSTOMER")
    private String role;

    @Schema(description = "Account status", example = "ACTIVE")
    private String status;

    @Schema(description = "Profile completion percentage", example = "25")
    private Integer profileCompletion;
}
