package com.teadelivery.notification.tracking.model;

import com.teadelivery.notification.shared.dto.NotificationRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Rate limit tracking entity for notification throttling.
 * Follows coding standards with comprehensive rate limiting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_rate_limits", indexes = {
    @Index(name = "idx_rate_limits_identifier_type", columnList = "identifier, type"),
    @Index(name = "idx_rate_limits_window_start", columnList = "window_start")
})
public class NotificationRateLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identifier", nullable = false)
    private String identifier; // email or phone number

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationRequest.NotificationType type;

    @Column(name = "count", nullable = false)
    @Builder.Default
    private Integer count = 1;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Creates rate limit entry.
     * 
     * @param identifier email or phone
     * @param type notification type
     * @return rate limit entry
     */
    public static NotificationRateLimit create(String identifier, NotificationRequest.NotificationType type) {
        return NotificationRateLimit.builder()
                .identifier(identifier)
                .type(type)
                .count(1)
                .windowStart(Instant.now())
                .build();
    }

    /**
     * Increments the count.
     */
    public void increment() {
        this.count++;
    }

    /**
     * Resets the count for new window.
     */
    public void resetWindow() {
        this.count = 1;
        this.windowStart = Instant.now();
    }
}
