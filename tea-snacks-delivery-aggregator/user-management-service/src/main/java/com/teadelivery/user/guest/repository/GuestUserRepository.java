package com.teadelivery.user.guest.repository;

import com.teadelivery.user.guest.model.GuestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for GuestUser entity operations.
 * Follows coding standards with proper method naming and documentation.
 */
@Repository
public interface GuestUserRepository extends JpaRepository<GuestUser, UUID> {

    /**
     * Find guest user by device ID.
     * 
     * @param deviceId the device identifier
     * @return optional guest user
     */
    Optional<GuestUser> findByDeviceId(String deviceId);

    /**
     * Find guest user by session token.
     * 
     * @param sessionToken the session token
     * @return optional guest user
     */
    Optional<GuestUser> findBySessionToken(String sessionToken);

    /**
     * Find active guest user by device ID.
     * 
     * @param deviceId the device identifier
     * @return optional active guest user
     */
    @Query("SELECT gu FROM GuestUser gu WHERE gu.deviceId = :deviceId AND gu.isActive = true AND gu.expiresAt > :now")
    Optional<GuestUser> findActiveByDeviceId(@Param("deviceId") String deviceId, @Param("now") LocalDateTime now);

    /**
     * Find active guest user by session token.
     * 
     * @param sessionToken the session token
     * @return optional active guest user
     */
    @Query("SELECT gu FROM GuestUser gu WHERE gu.sessionToken = :sessionToken AND gu.isActive = true AND gu.expiresAt > :now")
    Optional<GuestUser> findActiveBySessionToken(@Param("sessionToken") String sessionToken, @Param("now") LocalDateTime now);

    /**
     * Check if device has active guest session.
     * 
     * @param deviceId the device identifier
     * @return true if active session exists
     */
    @Query("SELECT COUNT(gu) > 0 FROM GuestUser gu WHERE gu.deviceId = :deviceId AND gu.isActive = true AND gu.expiresAt > :now")
    boolean existsActiveByDeviceId(@Param("deviceId") String deviceId, @Param("now") LocalDateTime now);

    /**
     * Find expired guest sessions.
     * 
     * @param expiryTime the expiry time threshold
     * @return list of expired guest users
     */
    @Query("SELECT gu FROM GuestUser gu WHERE gu.expiresAt <= :expiryTime AND gu.isActive = true")
    List<GuestUser> findExpiredSessions(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * Find guest users created in the last 24 hours by device ID.
     * 
     * @param deviceId the device identifier
     * @param since the time since when to check
     * @return list of recent guest users
     */
    @Query("SELECT gu FROM GuestUser gu WHERE gu.deviceId = :deviceId AND gu.createdAt >= :since")
    List<GuestUser> findByDeviceIdAndCreatedAtAfter(@Param("deviceId") String deviceId, @Param("since") LocalDateTime since);

    /**
     * Count active guest sessions.
     * 
     * @param now current time
     * @return count of active sessions
     */
    @Query("SELECT COUNT(gu) FROM GuestUser gu WHERE gu.isActive = true AND gu.expiresAt > :now")
    long countActiveSessions(@Param("now") LocalDateTime now);

    /**
     * Find guest users that need conversion prompts.
     * 
     * @param actionThreshold the action count threshold
     * @param now current time
     * @return list of guest users needing conversion prompts
     */
    @Query("SELECT gu FROM GuestUser gu WHERE gu.actionCount >= :actionThreshold AND gu.isActive = true AND gu.expiresAt > :now")
    List<GuestUser> findNeedingConversionPrompts(@Param("actionThreshold") Integer actionThreshold, @Param("now") LocalDateTime now);
} 