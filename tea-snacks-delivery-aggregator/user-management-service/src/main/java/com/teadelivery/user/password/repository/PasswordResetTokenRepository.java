package com.teadelivery.user.password.repository;

import com.teadelivery.user.password.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PasswordResetToken entity.
 * Follows coding standards with comprehensive data access methods.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    /**
     * Find valid token by token string.
     * 
     * @param token token string
     * @return optional password reset token
     */
    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    
    /**
     * Find token by user ID and email.
     * 
     * @param userId user ID
     * @param email email address
     * @return optional password reset token
     */
    Optional<PasswordResetToken> findByUserIdAndEmailAndUsedFalseAndExpiresAtAfter(
            UUID userId, String email, LocalDateTime now);
    
    /**
     * Find all tokens for a user.
     * 
     * @param userId user ID
     * @return list of password reset tokens
     */
    List<PasswordResetToken> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find all expired tokens.
     * 
     * @param now current time
     * @return list of expired tokens
     */
    List<PasswordResetToken> findByExpiresAtBefore(LocalDateTime now);
    
    /**
     * Mark token as used.
     * 
     * @param token token string
     */
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.token = :token")
    void markTokenAsUsed(@Param("token") String token);
    
    /**
     * Delete expired tokens.
     * 
     * @param now current time
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Count active tokens for user.
     * 
     * @param userId user ID
     * @param now current time
     * @return count of active tokens
     */
    long countByUserIdAndUsedFalseAndExpiresAtAfter(UUID userId, LocalDateTime now);
} 