package com.teadelivery.notification.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Notification request model for sending notifications.
 * Follows coding standards with comprehensive notification data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private UUID userId;
    private NotificationType type;
    private String template;
    private String recipient;
    private String subject; // For email
    private Map<String, Object> variables;
    private Priority priority;
    private boolean trackDelivery;

    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
