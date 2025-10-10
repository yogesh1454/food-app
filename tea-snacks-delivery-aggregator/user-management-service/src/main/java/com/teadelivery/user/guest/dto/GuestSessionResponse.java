package com.teadelivery.user.guest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for guest session information response.
 * Follows coding standards with proper structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestSessionResponse {

    private boolean success;
    private String message;
    private GuestSessionData data;

    /**
     * Guest session data structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestSessionData {
        private String guestUserId;
        private String sessionStatus;
        private LocalDateTime expiryTime;
        private String timeRemaining;
        private List<String> limitations;
        private ConversionPrompts conversionPrompts;
    }

    /**
     * Conversion prompts information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionPrompts {
        private Integer showAfterActions;
        private Integer currentActionCount;
    }
} 