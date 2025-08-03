package com.teadelivery.user.guest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for guest user creation response.
 * Follows coding standards with proper structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestUserResponse {

    private boolean success;
    private String message;
    private GuestUserData data;

    /**
     * Guest user data structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestUserData {
        private String guestUserId;
        private String sessionToken;
        private String userType;
        private LocalDateTime expiryTime;
        private List<String> limitations;
        private Integer actionCount;
        private Integer conversionPromptsShown;
    }
} 