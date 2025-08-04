package com.teadelivery.user.profile.controller;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.service.ProfileService;
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
} 