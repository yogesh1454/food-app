package com.teadelivery.user.password.repository;

import com.teadelivery.user.password.model.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for PasswordHistory entity.
 * Follows coding standards with comprehensive data access methods.
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {
    
    /**
     * Find password history by user ID.
     * 
     * @param userId user ID
     * @return list of password history entries
     */
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find recent password history by user ID (last 5 entries).
     * 
     * @param userId user ID
     * @return list of recent password history entries
     */
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.userId = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findRecentPasswordHistory(@Param("userId") UUID userId);
    
    /**
     * Check if password hash exists in user's history.
     * 
     * @param userId user ID
     * @param passwordHash password hash
     * @return true if password hash exists in history
     */
    boolean existsByUserIdAndPasswordHash(UUID userId, String passwordHash);
    
    /**
     * Count password history entries for user.
     * 
     * @param userId user ID
     * @return count of password history entries
     */
    long countByUserId(UUID userId);
    
    /**
     * Find password history by user ID and change reason.
     * 
     * @param userId user ID
     * @param changeReason change reason
     * @return list of password history entries
     */
    List<PasswordHistory> findByUserIdAndChangeReasonOrderByCreatedAtDesc(UUID userId, String changeReason);
} 