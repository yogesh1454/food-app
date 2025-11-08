package com.teadelivery.user.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for guest user creation request.
 * Follows coding standards with proper validation annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestUserRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private String userAgent;

    private String ipAddress;

    private SessionMetadata sessionMetadata;

    /**
     * Session metadata for guest user creation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionMetadata {
        private String platform;
        private String version;
    }
} 