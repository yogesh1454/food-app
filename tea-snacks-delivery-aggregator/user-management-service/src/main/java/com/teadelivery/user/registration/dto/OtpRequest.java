package com.teadelivery.user.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP generation request.
 * Follows coding standards with proper validation and documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Phone number must be in international format (e.g., +91-9876543210)"
    )
    private String phoneNumber;
} 