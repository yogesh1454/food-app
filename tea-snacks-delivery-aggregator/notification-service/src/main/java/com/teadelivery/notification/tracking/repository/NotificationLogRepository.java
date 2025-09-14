package com.teadelivery.notification.tracking.repository;

import com.teadelivery.notification.tracking.model.NotificationLog;
import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for notification log operations.
 * Follows coding standards with comprehensive query methods.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    /**
     * Finds notification logs by user ID.
     * 
     * @param userId user ID
     * @param pageable pagination info
     * @return page of notification logs
     */
    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Finds notification logs by type and status.
     * 
     * @param type notification type
     * @param status notification status
     * @param pageable pagination info
     * @return page of notification logs
     */
    Page<NotificationLog> findByTypeAndStatusOrderByCreatedAtDesc(
            NotificationRequest.NotificationType type,
            NotificationResponse.NotificationStatus status,
            Pageable pageable);

    /**
     * Finds failed notifications for retry.
     * 
     * @param maxRetries maximum retry count
     * @param before created before this time
     * @return list of failed notifications
     */
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.status = 'FAILED' " +
           "AND nl.retryCount < :maxRetries " +
           "AND nl.createdAt < :before " +
           "ORDER BY nl.createdAt ASC")
    List<NotificationLog> findFailedNotificationsForRetry(
            @Param("maxRetries") int maxRetries,
            @Param("before") Instant before);

    /**
     * Counts notifications by type and status within time range.
     * 
     * @param type notification type
     * @param status notification status
     * @param from start time
     * @param to end time
     * @return count of notifications
     */
    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.type = :type " +
           "AND nl.status = :status " +
           "AND nl.createdAt BETWEEN :from AND :to")
    long countByTypeAndStatusAndCreatedAtBetween(
            @Param("type") NotificationRequest.NotificationType type,
            @Param("status") NotificationResponse.NotificationStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to);

    /**
     * Finds notifications by recipient and type within time range.
     * 
     * @param recipient recipient (email or phone)
     * @param type notification type
     * @param from start time
     * @return list of notifications
     */
    List<NotificationLog> findByRecipientAndTypeAndCreatedAtAfter(
            String recipient,
            NotificationRequest.NotificationType type,
            Instant from);

    /**
     * Deletes old notification logs.
     * 
     * @param before delete logs created before this time
     * @return number of deleted records
     */
    @Query("DELETE FROM NotificationLog nl WHERE nl.createdAt < :before")
    int deleteByCreatedAtBefore(@Param("before") Instant before);
}
