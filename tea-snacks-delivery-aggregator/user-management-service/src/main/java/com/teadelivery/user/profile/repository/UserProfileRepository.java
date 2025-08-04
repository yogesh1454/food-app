package com.teadelivery.user.profile.repository;

import com.teadelivery.user.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserProfile entity.
 * Follows coding standards with comprehensive data access methods.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    /**
     * Find user profile by user ID.
     * 
     * @param userId user ID
     * @return optional user profile
     */
    Optional<UserProfile> findByUserId(UUID userId);
    
    /**
     * Check if user profile exists by user ID.
     * 
     * @param userId user ID
     * @return true if exists, false otherwise
     */
    boolean existsByUserId(UUID userId);
} 