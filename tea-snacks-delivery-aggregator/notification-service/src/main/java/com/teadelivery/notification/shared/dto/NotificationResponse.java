package com.teadelivery.notification.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification response model for tracking notification status.
 * Follows coding standards with comprehensive status tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID notificationId;
    private UUID userId;
    private NotificationStatus status;
    private String message;
    private String providerResponse;
    private String providerMessageId;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant failedAt;
    private int retryCount;
    private String errorMessage;

    public enum NotificationStatus {
        PENDING,
        PROCESSING,
        SENT,
        DELIVERED,
        FAILED,
        CANCELLED
    }

    /**
     * Creates a successful notification response.
     * 
     * @param notificationId notification ID
     * @param userId user ID
     * @param providerMessageId provider message ID
     * @return successful response
     */
    public static NotificationResponse success(UUID notificationId, UUID userId, String providerMessageId) {
        return NotificationResponse.builder()
                .notificationId(notificationId)
                .userId(userId)
                .status(NotificationStatus.SENT)
                .message("Notification sent successfully")
                .providerMessageId(providerMessageId)
                .sentAt(Instant.now())
                .retryCount(0)
                .build();
    }

    /**
     * Creates a failed notification response.
     * 
     * @param notificationId notification ID
     * @param userId user ID
     * @param errorMessage error message
     * @param retryCount retry count
     * @return failed response
     */
    public static NotificationResponse failure(UUID notificationId, UUID userId, String errorMessage, int retryCount) {
        return NotificationResponse.builder()
                .notificationId(notificationId)
                .userId(userId)
                .status(NotificationStatus.FAILED)
                .message("Notification failed")
                .errorMessage(errorMessage)
                .failedAt(Instant.now())
                .retryCount(retryCount)
                .build();
    }
}
