package com.teadelivery.user.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP verification request.
 * Follows coding standards with proper validation and documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Phone number must be in international format (e.g., +91-9876543210)"
    )
    private String phoneNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(
        regexp = "^\\d{6}$",
        message = "OTP must be a 6-digit number"
    )
    private String otp;

    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Email must be in valid format"
    )
    private String email;
} 