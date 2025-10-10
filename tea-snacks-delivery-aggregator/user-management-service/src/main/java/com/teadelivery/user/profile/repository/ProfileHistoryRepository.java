package com.teadelivery.user.profile.repository;

import com.teadelivery.user.profile.model.ProfileHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ProfileHistory entity.
 * Follows coding standards with comprehensive data access methods.
 */
@Repository
public interface ProfileHistoryRepository extends JpaRepository<ProfileHistory, UUID> {
    
    /**
     * Find profile history by user ID.
     * 
     * @param userId user ID
     * @return list of profile history entries
     */
    List<ProfileHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find profile history by user ID with pagination.
     * 
     * @param userId user ID
     * @param pageable pagination parameters
     * @return page of profile history entries
     */
    Page<ProfileHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find profile history by user ID and field name.
     * 
     * @param userId user ID
     * @param fieldName field name
     * @return list of profile history entries
     */
    List<ProfileHistory> findByUserIdAndFieldNameOrderByCreatedAtDesc(UUID userId, String fieldName);
    
    /**
     * Find profile history by user ID and change type.
     * 
     * @param userId user ID
     * @param changeType change type
     * @return list of profile history entries
     */
    List<ProfileHistory> findByUserIdAndChangeTypeOrderByCreatedAtDesc(UUID userId, ProfileHistory.ChangeType changeType);
    
    /**
     * Find profile history by user ID and date range.
     * 
     * @param userId user ID
     * @param startDate start date
     * @param endDate end date
     * @return list of profile history entries
     */
    List<ProfileHistory> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count profile history entries by user ID.
     * 
     * @param userId user ID
     * @return count of profile history entries
     */
    long countByUserId(UUID userId);
    
    /**
     * Find recent profile changes by user ID.
     * 
     * @param userId user ID
     * @param limit limit of results
     * @return list of recent profile history entries
     */
    List<ProfileHistory> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
} 