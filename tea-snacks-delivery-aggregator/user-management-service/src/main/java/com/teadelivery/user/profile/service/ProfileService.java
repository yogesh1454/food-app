package com.teadelivery.user.profile.service;

import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.model.UserProfile;
import com.teadelivery.user.profile.repository.UserProfileRepository;
import com.teadelivery.user.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for user profile management.
 * Follows coding standards with comprehensive profile operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileStorageService fileStorageService;

    /**
     * Get user profile by user ID.
     * 
     * @param userId user ID
     * @return profile response
     */
    @Transactional(readOnly = true)
    public ProfileResponse getUserProfile(UUID userId) {
        log.info("Getting profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUserProfile(user));
        
        return buildProfileResponse(user, userProfile);
    }

    /**
     * Get current user's profile.
     * 
     * @return profile response
     */
    @Transactional(readOnly = true)
    public ProfileResponse getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByEmailOrPhoneNumber(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return getUserProfile(user.getId());
    }

    /**
     * Update user profile.
     * 
     * @param userId user ID
     * @param request profile update request
     * @return updated profile response
     */
    @Transactional
    public ProfileResponse updateUserProfile(UUID userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUserProfile(user));
        
        // Update basic user information
        updateBasicUserInfo(user, request);
        
        // Update profile information
        updateProfileInfo(userProfile, request);
        
        // Save updated entities
        userRepository.save(user);
        userProfileRepository.save(userProfile);
        
        // Update profile completion percentage
        updateProfileCompletionPercentage(user, userProfile);
        
        log.info("Profile updated successfully for user: {}", userId);
        
        return buildProfileResponse(user, userProfile);
    }

    /**
     * Update current user's profile.
     * 
     * @param request profile update request
     * @return updated profile response
     */
    @Transactional
    public ProfileResponse updateCurrentUserProfile(ProfileUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByEmailOrPhoneNumber(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return updateUserProfile(user.getId(), request);
    }

    /**
     * Upload profile picture.
     * 
     * @param userId user ID
     * @param file profile picture file
     * @return profile picture URL
     */
    @Transactional
    public String uploadProfilePicture(UUID userId, MultipartFile file) {
        log.info("Uploading profile picture for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUserProfile(user));
        
        try {
            // Validate file
            validateProfilePicture(file);
            
            // Upload file and get URL
            String fileUrl = fileStorageService.uploadFile(file, "profile-pictures");
            
            // Update profile with new picture URL
            userProfile.setAvatarUrl(fileUrl);
            userProfileRepository.save(userProfile);
            
            // Update profile completion percentage
            updateProfileCompletionPercentage(user, userProfile);
            
            log.info("Profile picture uploaded successfully for user: {}", userId);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload profile picture for user: {}", userId, e);
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    /**
     * Update current user's profile picture.
     * 
     * @param file profile picture file
     * @return profile picture URL
     */
    @Transactional
    public String uploadCurrentUserProfilePicture(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByEmailOrPhoneNumber(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return uploadProfilePicture(user.getId(), file);
    }

    /**
     * Create default user profile.
     * 
     * @param user user entity
     * @return default user profile
     */
    private UserProfile createDefaultUserProfile(User user) {
        log.debug("Creating default profile for user: {}", user.getId());
        
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .firstName(extractFirstName(user.getName()))
                .lastName(extractLastName(user.getName()))
                .preferredLanguage("en")
                .timezone("UTC")
                .build();
        
        return userProfileRepository.save(userProfile);
    }

    /**
     * Update basic user information.
     * 
     * @param user user entity
     * @param request update request
     */
    private void updateBasicUserInfo(User user, ProfileUpdateRequest request) {
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Email change requires verification
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
            log.info("Email updated for user: {}, verification required", user.getId());
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            // Phone change requires verification
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPhoneVerified(false);
            log.info("Phone number updated for user: {}, verification required", user.getId());
        }
    }

    /**
     * Update profile information.
     * 
     * @param userProfile user profile entity
     * @param request update request
     */
    private void updateProfileInfo(UserProfile userProfile, ProfileUpdateRequest request) {
        if (request.getFirstName() != null) {
            userProfile.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            userProfile.setLastName(request.getLastName());
        }
        
        if (request.getDateOfBirth() != null) {
            userProfile.setDateOfBirth(request.getDateOfBirth());
        }
        
        if (request.getGender() != null) {
            userProfile.setGender(UserProfile.Gender.valueOf(request.getGender()));
        }
        
        if (request.getBio() != null) {
            userProfile.setBio(request.getBio());
        }
        
        if (request.getPreferredLanguage() != null) {
            userProfile.setPreferredLanguage(request.getPreferredLanguage());
        }
        
        if (request.getTimezone() != null) {
            userProfile.setTimezone(request.getTimezone());
        }
        
        if (request.getNotificationPreferences() != null) {
            userProfile.setNotificationPreferences(request.getNotificationPreferences());
        }
        
        // Update role-specific information
        updateRoleSpecificInfo(userProfile, request);
    }

    /**
     * Update role-specific profile information.
     * 
     * @param userProfile user profile entity
     * @param request update request
     */
    private void updateRoleSpecificInfo(UserProfile userProfile, ProfileUpdateRequest request) {
        User user = userProfile.getUser();
        
        switch (user.getRole()) {
            case VENDOR:
                if (request.getBusinessDetails() != null) {
                    userProfile.setBusinessName(request.getBusinessDetails().getBusinessName());
                    userProfile.setBusinessType(request.getBusinessDetails().getBusinessType());
                    userProfile.setBusinessRegistrationNumber(request.getBusinessDetails().getBusinessRegistrationNumber());
                    userProfile.setGstNumber(request.getBusinessDetails().getGstNumber());
                }
                break;
                
            case DELIVERY_PARTNER:
                if (request.getVehicleDetails() != null) {
                    // Vehicle details would be stored in a separate entity
                    // For now, we'll store in notification preferences as JSON
                    userProfile.setNotificationPreferences("vehicle:" + request.getVehicleDetails().getVehicleType() + 
                            ":" + request.getVehicleDetails().getVehicleNumber());
                }
                break;
                
            case CUSTOMER:
                if (request.getCompanyDetails() != null) {
                    // Company details would be stored in a separate entity
                    // For now, we'll store in notification preferences as JSON
                    userProfile.setNotificationPreferences("company:" + request.getCompanyDetails().getCompanyName() + 
                            ":" + request.getCompanyDetails().getInternalDeliveryPoint());
                }
                break;
        }
    }

    /**
     * Update profile completion percentage.
     * 
     * @param user user entity
     * @param userProfile user profile entity
     */
    private void updateProfileCompletionPercentage(User user, UserProfile userProfile) {
        int completionPercentage = calculateProfileCompletion(user, userProfile);
        user.setProfileCompletionPercentage(completionPercentage);
        userRepository.save(user);
    }

    /**
     * Calculate profile completion percentage.
     * 
     * @param user user entity
     * @param userProfile user profile entity
     * @return completion percentage
     */
    private int calculateProfileCompletion(User user, UserProfile userProfile) {
        int totalFields = 10; // Total number of profile fields
        int completedFields = 0;
        
        // Basic user fields
        if (user.getEmail() != null && !user.getEmail().isEmpty()) completedFields++;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) completedFields++;
        if (user.getName() != null && !user.getName().isEmpty()) completedFields++;
        
        // Profile fields
        if (userProfile.getFirstName() != null && !userProfile.getFirstName().isEmpty()) completedFields++;
        if (userProfile.getLastName() != null && !userProfile.getLastName().isEmpty()) completedFields++;
        if (userProfile.getDateOfBirth() != null) completedFields++;
        if (userProfile.getGender() != null) completedFields++;
        if (userProfile.getAvatarUrl() != null && !userProfile.getAvatarUrl().isEmpty()) completedFields++;
        if (userProfile.getBio() != null && !userProfile.getBio().isEmpty()) completedFields++;
        if (userProfile.getAddressLine1() != null && !userProfile.getAddressLine1().isEmpty()) completedFields++;
        
        return Math.round((float) completedFields / totalFields * 100);
    }

    /**
     * Build profile response from user and profile entities.
     * 
     * @param user user entity
     * @param userProfile user profile entity
     * @return profile response
     */
    private ProfileResponse buildProfileResponse(User user, UserProfile userProfile) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .fullName(user.getName())
                .profilePictureUrl(userProfile.getAvatarUrl())
                .dateOfBirth(userProfile.getDateOfBirth())
                .gender(userProfile.getGender() != null ? userProfile.getGender().name() : null)
                .bio(userProfile.getBio())
                .profileCompletionPercentage(user.getProfileCompletionPercentage())
                .role(user.getRole().name())
                .userType(user.getUserType().name())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .addresses(buildAddressList(userProfile))
                .companyDetails(buildCompanyDetails(userProfile))
                .businessDetails(buildBusinessDetails(userProfile))
                .vehicleDetails(buildVehicleDetails(userProfile))
                .preferredLanguage(userProfile.getPreferredLanguage())
                .timezone(userProfile.getTimezone())
                .notificationPreferences(userProfile.getNotificationPreferences())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .build();
    }

    /**
     * Build address list from user profile.
     * 
     * @param userProfile user profile entity
     * @return list of addresses
     */
    private List<ProfileResponse.AddressDto> buildAddressList(UserProfile userProfile) {
        List<ProfileResponse.AddressDto> addresses = new ArrayList<>();
        
        if (userProfile.getAddressLine1() != null && !userProfile.getAddressLine1().isEmpty()) {
            addresses.add(ProfileResponse.AddressDto.builder()
                    .id(userProfile.getId())
                    .type("HOME")
                    .street(userProfile.getAddressLine1())
                    .city(userProfile.getCity())
                    .state(userProfile.getState())
                    .postalCode(userProfile.getPostalCode())
                    .country(userProfile.getCountry())
                    .latitude(userProfile.getLatitude())
                    .longitude(userProfile.getLongitude())
                    .build());
        }
        
        return addresses;
    }

    /**
     * Build company details from user profile.
     * 
     * @param userProfile user profile entity
     * @return company details
     */
    private ProfileResponse.CompanyDetailsDto buildCompanyDetails(UserProfile userProfile) {
        if (userProfile.getNotificationPreferences() != null && 
            userProfile.getNotificationPreferences().startsWith("company:")) {
            String[] parts = userProfile.getNotificationPreferences().split(":");
            if (parts.length >= 3) {
                return ProfileResponse.CompanyDetailsDto.builder()
                        .companyName(parts[1])
                        .internalDeliveryPoint(parts[2])
                        .build();
            }
        }
        return null;
    }

    /**
     * Build business details from user profile.
     * 
     * @param userProfile user profile entity
     * @return business details
     */
    private ProfileResponse.BusinessDetailsDto buildBusinessDetails(UserProfile userProfile) {
        if (userProfile.getBusinessName() != null) {
            return ProfileResponse.BusinessDetailsDto.builder()
                    .businessName(userProfile.getBusinessName())
                    .businessType(userProfile.getBusinessType())
                    .businessRegistrationNumber(userProfile.getBusinessRegistrationNumber())
                    .gstNumber(userProfile.getGstNumber())
                    .build();
        }
        return null;
    }

    /**
     * Build vehicle details from user profile.
     * 
     * @param userProfile user profile entity
     * @return vehicle details
     */
    private ProfileResponse.VehicleDetailsDto buildVehicleDetails(UserProfile userProfile) {
        if (userProfile.getNotificationPreferences() != null && 
            userProfile.getNotificationPreferences().startsWith("vehicle:")) {
            String[] parts = userProfile.getNotificationPreferences().split(":");
            if (parts.length >= 3) {
                return ProfileResponse.VehicleDetailsDto.builder()
                        .vehicleType(parts[1])
                        .vehicleNumber(parts[2])
                        .build();
            }
        }
        return null;
    }

    /**
     * Extract first name from full name.
     * 
     * @param fullName full name
     * @return first name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String[] parts = fullName.trim().split(" ", 2);
        return parts[0];
    }

    /**
     * Extract last name from full name.
     * 
     * @param fullName full name
     * @return last name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String[] parts = fullName.trim().split(" ", 2);
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * Validate profile picture file.
     * 
     * @param file profile picture file
     */
    private void validateProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Profile picture file is required");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new RuntimeException("Profile picture file size must be less than 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Profile picture must be an image file");
        }
    }
} 