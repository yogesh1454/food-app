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
@Schema(description = "Response payload for phone registration")
public class PhoneRegistrationResponse {

    @Schema(description = "Indicates if the operation was successful", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "OTP sent to phone number")
    private String message;

    @Schema(description = "User's phone number", example = "+91-9876543210")
    private String phoneNumber;

    @Schema(description = "Session ID for OTP verification", example = "f39b63d9-c243-41f9-93e0-e18ba50a6583")
    private String sessionId;

    @Schema(description = "OTP expiry time in minutes", example = "5")
    private Integer expiryMinutes;
} 