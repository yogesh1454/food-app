package com.teadelivery.notification.tracking.repository;

import com.teadelivery.notification.tracking.model.NotificationRateLimit;
import com.teadelivery.notification.shared.dto.NotificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for notification rate limit operations.
 * Follows coding standards with comprehensive rate limiting queries.
 */
@Repository
public interface NotificationRateLimitRepository extends JpaRepository<NotificationRateLimit, UUID> {

    /**
     * Finds rate limit entry by identifier and type.
     * 
     * @param identifier email or phone number
     * @param type notification type
     * @return optional rate limit entry
     */
    Optional<NotificationRateLimit> findByIdentifierAndType(
            String identifier,
            NotificationRequest.NotificationType type);

    /**
     * Finds active rate limit entry within time window.
     * 
     * @param identifier email or phone number
     * @param type notification type
     * @param windowStart start of current window
     * @return optional rate limit entry
     */
    Optional<NotificationRateLimit> findByIdentifierAndTypeAndWindowStartAfter(
            String identifier,
            NotificationRequest.NotificationType type,
            Instant windowStart);

    /**
     * Deletes expired rate limit entries.
     * 
     * @param before delete entries with window start before this time
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM NotificationRateLimit nrl WHERE nrl.windowStart < :before")
    int deleteByWindowStartBefore(@Param("before") Instant before);

    /**
     * Counts active rate limit entries for identifier and type.
     * 
     * @param identifier email or phone number
     * @param type notification type
     * @param windowStart start of current window
     * @return count of notifications in current window
     */
    @Query("SELECT COALESCE(SUM(nrl.count), 0) FROM NotificationRateLimit nrl " +
           "WHERE nrl.identifier = :identifier " +
           "AND nrl.type = :type " +
           "AND nrl.windowStart >= :windowStart")
    int countNotificationsInWindow(
            @Param("identifier") String identifier,
            @Param("type") NotificationRequest.NotificationType type,
            @Param("windowStart") Instant windowStart);
}
