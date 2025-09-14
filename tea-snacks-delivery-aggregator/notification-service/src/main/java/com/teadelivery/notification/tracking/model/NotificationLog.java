package com.teadelivery.notification.tracking.model;

import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification log entity for tracking notification history.
 * Follows coding standards with comprehensive audit trail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_logs", indexes = {
    @Index(name = "idx_notification_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_logs_type", columnList = "type"),
    @Index(name = "idx_notification_logs_status", columnList = "status"),
    @Index(name = "idx_notification_logs_created_at", columnList = "created_at")
})
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationRequest.NotificationType type;

    @Column(name = "template", nullable = false, length = 50)
    private String template;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject")
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationResponse.NotificationStatus status;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    @Builder.Default
    private NotificationRequest.Priority priority = NotificationRequest.Priority.NORMAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    /**
     * Creates notification log from request.
     * 
     * @param request notification request
     * @return notification log
     */
    public static NotificationLog fromRequest(NotificationRequest request) {
        return NotificationLog.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .template(request.getTemplate())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .status(NotificationResponse.NotificationStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : NotificationRequest.Priority.NORMAL)
                .build();
    }

    /**
     * Updates log with response data.
     * 
     * @param response notification response
     */
    public void updateFromResponse(NotificationResponse response) {
        this.status = response.getStatus();
        this.providerResponse = response.getProviderResponse();
        this.providerMessageId = response.getProviderMessageId();
        this.sentAt = response.getSentAt();
        this.deliveredAt = response.getDeliveredAt();
        this.failedAt = response.getFailedAt();
        this.retryCount = response.getRetryCount();
        this.errorMessage = response.getErrorMessage();
    }
}
