package com.teadelivery.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP verification response.
 * Follows coding standards with proper documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationResponse {

    private boolean success;
    private String message;
    private Integer attemptsRemaining;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String userType;
    private Integer profileCompletion;
} 