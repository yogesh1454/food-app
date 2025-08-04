package com.teadelivery.user.profile.controller;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.service.ProfileService;
import com.teadelivery.user.profile.service.ProfileVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for user profile management.
 * Follows coding standards with comprehensive REST endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile Management", description = "APIs for user profile management")
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileVerificationService profileVerificationService;

    /**
     * Get user profile by user ID.
     * 
     * @param userId user ID
     * @return profile response
     */
    @GetMapping("/{userId}/profile")
    @HasPermission(resource = "profile", action = "view", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Get user profile", description = "Retrieves user profile by user ID")
    @ApiResponse(
        responseCode = "200",
        description = "Profile retrieved successfully",
        content = @Content(schema = @Schema(implementation = ProfileResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable UUID userId) {
        log.info("Getting profile for user: {}", userId);
        
        ProfileResponse profile = profileService.getUserProfile(userId);
        
        log.info("Profile retrieved successfully for user: {}", userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get current user's profile.
     * 
     * @return profile response
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves current user's profile")
    @ApiResponse(
        responseCode = "200",
        description = "Profile retrieved successfully",
        content = @Content(schema = @Schema(implementation = ProfileResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        log.info("Getting current user profile");
        
        ProfileResponse profile = profileService.getCurrentUserProfile();
        
        log.info("Current user profile retrieved successfully");
        return ResponseEntity.ok(profile);
    }

    /**
     * Update user profile by user ID.
     * 
     * @param userId user ID
     * @param request profile update request
     * @return updated profile response
     */
    @PutMapping("/{userId}/profile")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Update user profile", description = "Updates user profile by user ID")
    @ApiResponse(
        responseCode = "200",
        description = "Profile updated successfully",
        content = @Content(schema = @Schema(implementation = ProfileResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);
        
        ProfileResponse updatedProfile = profileService.updateUserProfile(userId, request);
        
        log.info("Profile updated successfully for user: {}", userId);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update current user's profile.
     * 
     * @param request profile update request
     * @return updated profile response
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Updates current user's profile")
    @ApiResponse(
        responseCode = "200",
        description = "Profile updated successfully",
        content = @Content(schema = @Schema(implementation = ProfileResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<ProfileResponse> updateCurrentUserProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Updating current user profile");
        
        ProfileResponse updatedProfile = profileService.updateCurrentUserProfile(request);
        
        log.info("Current user profile updated successfully");
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Upload profile picture for user.
     * 
     * @param userId user ID
     * @param file profile picture file
     * @return profile picture URL
     */
    @PostMapping("/{userId}/profile/picture")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Upload profile picture", description = "Uploads profile picture for user")
    @ApiResponse(
        responseCode = "200",
        description = "Profile picture uploaded successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid file or file too large",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading profile picture for user: {}", userId);
        
        String profilePictureUrl = profileService.uploadProfilePicture(userId, file);
        
        Map<String, String> response = new HashMap<>();
        response.put("profile_picture_url", profilePictureUrl);
        response.put("message", "Profile picture uploaded successfully");
        
        log.info("Profile picture uploaded successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload profile picture for current user.
     * 
     * @param file profile picture file
     * @return profile picture URL
     */
    @PostMapping("/profile/picture")
    @Operation(summary = "Upload current user profile picture", description = "Uploads profile picture for current user")
    @ApiResponse(
        responseCode = "200",
        description = "Profile picture uploaded successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid file or file too large",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, String>> uploadCurrentUserProfilePicture(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading profile picture for current user");
        
        String profilePictureUrl = profileService.uploadCurrentUserProfilePicture(file);
        
        Map<String, String> response = new HashMap<>();
        response.put("profile_picture_url", profilePictureUrl);
        response.put("message", "Profile picture uploaded successfully");
        
        log.info("Profile picture uploaded successfully for current user");
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile completion percentage for user.
     * 
     * @param userId user ID
     * @return profile completion percentage
     */
    @GetMapping("/{userId}/profile/completion")
    @HasPermission(resource = "profile", action = "view", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Get profile completion", description = "Gets profile completion percentage for user")
    @ApiResponse(
        responseCode = "200",
        description = "Profile completion retrieved successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> getProfileCompletion(@PathVariable UUID userId) {
        log.info("Getting profile completion for user: {}", userId);
        
        ProfileResponse profile = profileService.getUserProfile(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("completion_percentage", profile.getProfileCompletionPercentage());
        response.put("message", "Profile completion retrieved successfully");
        
        log.info("Profile completion retrieved successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile completion percentage for current user.
     * 
     * @return profile completion percentage
     */
    @GetMapping("/profile/completion")
    @Operation(summary = "Get current user profile completion", description = "Gets profile completion percentage for current user")
    @ApiResponse(
        responseCode = "200",
        description = "Profile completion retrieved successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> getCurrentUserProfileCompletion() {
        log.info("Getting profile completion for current user");
        
        ProfileResponse profile = profileService.getCurrentUserProfile();
        
        Map<String, Object> response = new HashMap<>();
        response.put("completion_percentage", profile.getProfileCompletionPercentage());
        response.put("message", "Profile completion retrieved successfully");
        
        log.info("Profile completion retrieved successfully for current user");
        return ResponseEntity.ok(response);
    }

    /**
     * Request email verification for profile update.
     * 
     * @param userId user ID
     * @param newEmail new email address
     * @return verification response
     */
    @PostMapping("/{userId}/profile/verify-email")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Request email verification", description = "Requests email verification for profile update")
    @ApiResponse(
        responseCode = "200",
        description = "Verification email sent successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid email or email already in use",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> requestEmailVerification(
            @PathVariable UUID userId,
            @RequestParam String newEmail) {
        log.info("Requesting email verification for user: {}", userId);
        
        Map<String, Object> response = profileVerificationService.requestEmailVerification(userId, newEmail);
        
        log.info("Email verification requested successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Request phone verification for profile update.
     * 
     * @param userId user ID
     * @param newPhoneNumber new phone number
     * @return verification response
     */
    @PostMapping("/{userId}/profile/verify-phone")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Request phone verification", description = "Requests phone verification for profile update")
    @ApiResponse(
        responseCode = "200",
        description = "Verification SMS sent successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid phone number or phone already in use",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> requestPhoneVerification(
            @PathVariable UUID userId,
            @RequestParam String newPhoneNumber) {
        log.info("Requesting phone verification for user: {}", userId);
        
        Map<String, Object> response = profileVerificationService.requestPhoneVerification(userId, newPhoneNumber);
        
        log.info("Phone verification requested successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email with token.
     * 
     * @param userId user ID
     * @param verificationToken verification token
     * @return verification result
     */
    @PostMapping("/{userId}/profile/verify-email/confirm")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Confirm email verification", description = "Confirms email verification with token")
    @ApiResponse(
        responseCode = "200",
        description = "Email verified successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid verification token",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> confirmEmailVerification(
            @PathVariable UUID userId,
            @RequestParam String verificationToken) {
        log.info("Confirming email verification for user: {}", userId);
        
        Map<String, Object> response = profileVerificationService.verifyEmail(userId, verificationToken);
        
        log.info("Email verification confirmed successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify phone with OTP.
     * 
     * @param userId user ID
     * @param otp OTP code
     * @return verification result
     */
    @PostMapping("/{userId}/profile/verify-phone/confirm")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true, ownerIdParam = "userId")
    @Operation(summary = "Confirm phone verification", description = "Confirms phone verification with OTP")
    @ApiResponse(
        responseCode = "200",
        description = "Phone verified successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid OTP",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> confirmPhoneVerification(
            @PathVariable UUID userId,
            @RequestParam String otp) {
        log.info("Confirming phone verification for user: {}", userId);
        
        Map<String, Object> response = profileVerificationService.verifyPhone(userId, otp);
        
        log.info("Phone verification confirmed successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }
} 