package com.teadelivery.user.profile.repository;

import com.teadelivery.user.profile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find user by email or phone number
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByEmailOrPhoneNumber(@Param("identifier") String identifier);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    Optional<User> findActiveUsersByRole(@Param("role") User.Role role);
    
    /**
     * Find guest users that can be converted
     */
    @Query("SELECT u FROM User u WHERE u.userType = 'GUEST' AND u.status = 'ACTIVE'")
    Optional<User> findConvertibleGuestUsers();
}
