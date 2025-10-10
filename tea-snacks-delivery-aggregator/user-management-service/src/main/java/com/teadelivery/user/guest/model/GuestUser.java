package com.teadelivery.user.guest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a guest user in the system.
 * Follows coding standards with proper annotations and documentation.
 */
@Entity
@Table(name = "guest_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "platform")
    private String platform;

    @Column(name = "version")
    private String version;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "action_count", nullable = false)
    @Builder.Default
    private Integer actionCount = 0;

    @Column(name = "conversion_prompts_shown", nullable = false)
    @Builder.Default
    private Integer conversionPromptsShown = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "converted_to_user_id")
    private UUID convertedToUserId;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Checks if the guest session is expired.
     * 
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if the guest session is active and not expired.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive && !isExpired();
    }

    /**
     * Updates the last activity timestamp.
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Increments the action count.
     */
    public void incrementActionCount() {
        this.actionCount++;
    }

    /**
     * Increments the conversion prompts shown count.
     */
    public void incrementConversionPromptsShown() {
        this.conversionPromptsShown++;
    }

    /**
     * Marks the guest user as converted.
     * 
     * @param userId the ID of the registered user
     */
    public void markAsConverted(UUID userId) {
        this.convertedToUserId = userId;
        this.convertedAt = LocalDateTime.now();
        this.isActive = false;
    }
} 