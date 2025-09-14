package com.teadelivery.notification.tracking.service;

import com.teadelivery.notification.config.NotificationConfig;
import com.teadelivery.notification.tracking.model.NotificationRateLimit;
import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.tracking.repository.NotificationRateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Rate limiting service for notification throttling.
 * Follows coding standards with comprehensive rate limiting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final NotificationRateLimitRepository rateLimitRepository;
    private final NotificationConfig notificationConfig;

    /**
     * Checks if notification is allowed based on rate limits.
     * 
     * @param request notification request
     * @return true if allowed, false if rate limited
     */
    @Transactional
    public boolean isAllowed(NotificationRequest request) {
        try {
            String identifier = request.getRecipient();
            NotificationRequest.NotificationType type = request.getType();
            
            // Get rate limit configuration
            NotificationConfig.RateLimit rateLimit = getRateLimit(type);
            if (rateLimit == null) {
                log.debug("No rate limit configured for type: {}", type);
                return true;
            }
            
            // Calculate window start time
            Instant windowStart = getWindowStart(rateLimit.getPer());
            
            // Check current count in window
            int currentCount = rateLimitRepository.countNotificationsInWindow(identifier, type, windowStart);
            
            if (currentCount >= rateLimit.getMax()) {
                log.warn("Rate limit exceeded for {} ({}): {}/{} in window", 
                        identifier, type, currentCount, rateLimit.getMax());
                return false;
            }
            
            // Update or create rate limit entry
            updateRateLimitEntry(identifier, type, windowStart);
            
            log.debug("Rate limit check passed for {} ({}): {}/{} in window", 
                     identifier, type, currentCount + 1, rateLimit.getMax());
            
            return true;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for request: {}", request, e);
            // Allow on error to avoid blocking notifications
            return true;
        }
    }

    /**
     * Gets remaining quota for identifier and type.
     * 
     * @param identifier email or phone
     * @param type notification type
     * @return remaining quota
     */
    public int getRemainingQuota(String identifier, NotificationRequest.NotificationType type) {
        try {
            NotificationConfig.RateLimit rateLimit = getRateLimit(type);
            if (rateLimit == null) {
                return Integer.MAX_VALUE;
            }
            
            Instant windowStart = getWindowStart(rateLimit.getPer());
            int currentCount = rateLimitRepository.countNotificationsInWindow(identifier, type, windowStart);
            
            return Math.max(0, rateLimit.getMax() - currentCount);
            
        } catch (Exception e) {
            log.error("Error getting remaining quota for {} ({})", identifier, type, e);
            return 0;
        }
    }

    /**
     * Cleans up expired rate limit entries.
     * Should be called periodically.
     */
    @Transactional
    public void cleanupExpiredEntries() {
        try {
            // Clean up entries older than 24 hours
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
            int deletedCount = rateLimitRepository.deleteByWindowStartBefore(cutoff);
            
            if (deletedCount > 0) {
                log.info("Cleaned up {} expired rate limit entries", deletedCount);
            }
            
        } catch (Exception e) {
            log.error("Error cleaning up expired rate limit entries", e);
        }
    }

    /**
     * Gets rate limit configuration for notification type.
     * 
     * @param type notification type
     * @return rate limit configuration
     */
    private NotificationConfig.RateLimit getRateLimit(NotificationRequest.NotificationType type) {
        switch (type) {
            case EMAIL:
                return notificationConfig.getEmail().getRateLimit();
            case SMS:
                return notificationConfig.getSms().getRateLimit();
            default:
                return null;
        }
    }

    /**
     * Calculates window start time based on period.
     * 
     * @param period rate limit period (minute, hour, day)
     * @return window start time
     */
    private Instant getWindowStart(String period) {
        Instant now = Instant.now();
        
        switch (period.toLowerCase()) {
            case "minute":
                return now.truncatedTo(ChronoUnit.MINUTES);
            case "hour":
                return now.truncatedTo(ChronoUnit.HOURS);
            case "day":
                return now.truncatedTo(ChronoUnit.DAYS);
            default:
                log.warn("Unknown rate limit period: {}, defaulting to minute", period);
                return now.truncatedTo(ChronoUnit.MINUTES);
        }
    }

    /**
     * Updates or creates rate limit entry.
     * 
     * @param identifier email or phone
     * @param type notification type
     * @param windowStart window start time
     */
    private void updateRateLimitEntry(String identifier, NotificationRequest.NotificationType type, Instant windowStart) {
        Optional<NotificationRateLimit> existingEntry = 
            rateLimitRepository.findByIdentifierAndTypeAndWindowStartAfter(identifier, type, windowStart);
        
        if (existingEntry.isPresent()) {
            // Update existing entry
            NotificationRateLimit entry = existingEntry.get();
            entry.increment();
            rateLimitRepository.save(entry);
            
            log.debug("Updated rate limit entry for {} ({}): count = {}", 
                     identifier, type, entry.getCount());
        } else {
            // Create new entry
            NotificationRateLimit newEntry = NotificationRateLimit.create(identifier, type);
            newEntry.setWindowStart(windowStart);
            rateLimitRepository.save(newEntry);
            
            log.debug("Created new rate limit entry for {} ({})", identifier, type);
        }
    }
}
