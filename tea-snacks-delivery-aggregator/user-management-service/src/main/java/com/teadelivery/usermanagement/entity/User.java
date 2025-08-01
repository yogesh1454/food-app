package com.teadelivery.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "email", unique = true, length = 255)
    private String email;
    
    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;
    
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;
    
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Builder.Default
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;
    
    @Builder.Default
    @Column(name = "profile_completion_percentage")
    private Integer profileCompletionPercentage = 0;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    // Social login IDs
    @Column(name = "google_id", unique = true)
    private String googleId;
    
    @Column(name = "facebook_id", unique = true)
    private String facebookId;
    
    @Column(name = "instagram_id", unique = true)
    private String instagramId;
    
    @Column(name = "twitter_id", unique = true)
    private String twitterId;
    
    // Guest conversion fields
    @Column(name = "converted_from_guest_id")
    private UUID convertedFromGuestId;
    
    @Column(name = "conversion_date")
    private LocalDateTime conversionDate;
    
    @Builder.Default
    @Version
    @Column(name = "version")
    private Long version = 0L;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // One-to-One relationship with UserProfile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;
    
    public enum UserType {
        REGISTERED,
        GUEST
    }
    
    public enum Role {
        CUSTOMER,
        VENDOR,
        DELIVERY_PARTNER,
        ADMIN
    }
    
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_VERIFICATION
    }
}
