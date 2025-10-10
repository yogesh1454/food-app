package com.teadelivery.user.profile.repository;

import com.teadelivery.user.profile.model.OtpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for OTP session operations.
 * Follows coding standards with proper documentation.
 */
@Repository
public interface OtpSessionRepository extends JpaRepository<OtpSession, Long> {

    /**
     * Find OTP session by session ID.
     */
    Optional<OtpSession> findBySessionId(String sessionId);

    /**
     * Find active OTP session by phone number (not used and not expired).
     */
    @Query("SELECT o FROM OtpSession o WHERE o.phoneNumber = :phoneNumber AND o.used = false AND o.expiresAt > :now")
    Optional<OtpSession> findActiveSessionByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);

    /**
     * Count OTP requests for a phone number within a time window (for rate limiting).
     */
    @Query("SELECT COUNT(o) FROM OtpSession o WHERE o.phoneNumber = :phoneNumber AND o.createdAt >= :since")
    long countByPhoneNumberAndCreatedAtAfter(@Param("phoneNumber") String phoneNumber, @Param("since") LocalDateTime since);

    /**
     * Delete expired OTP sessions.
     */
    @Query("DELETE FROM OtpSession o WHERE o.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);
} 