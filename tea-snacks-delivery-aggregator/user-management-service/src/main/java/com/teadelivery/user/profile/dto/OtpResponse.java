package com.teadelivery.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP generation response.
 * Follows coding standards with proper documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private boolean success;
    private String message;
    private String sessionId;
    private Integer expiryMinutes;
    private Boolean resendAllowed;
    private Integer attemptsRemaining;
} 