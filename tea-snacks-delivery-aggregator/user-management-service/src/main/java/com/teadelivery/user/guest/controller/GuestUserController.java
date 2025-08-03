package com.teadelivery.user.guest.controller;

import com.teadelivery.user.guest.dto.GuestUserRequest;
import com.teadelivery.user.guest.dto.GuestUserResponse;
import com.teadelivery.user.guest.dto.GuestSessionResponse;
import com.teadelivery.user.guest.service.GuestUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for guest user operations.
 * Follows coding standards with proper REST endpoints, validation, and documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/guest")
@RequiredArgsConstructor
@Tag(name = "Guest User Management", description = "APIs for guest user creation and session management")
public class GuestUserController {

    private final GuestUserService guestUserService;

    /**
     * Creates a new guest user account.
     * 
     * @param request guest user creation request
     * @return guest user response
     */
    @PostMapping("/create")
    @Operation(
        summary = "Create guest user account",
        description = "Creates a new guest user account with device fingerprinting and session management"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Guest account created successfully",
        content = @Content(schema = @Schema(implementation = GuestUserResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid device ID or daily limit exceeded",
        content = @Content(schema = @Schema(implementation = GuestUserResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = GuestUserResponse.class))
    )
    public ResponseEntity<GuestUserResponse> createGuestUser(@Valid @RequestBody GuestUserRequest request) {
        log.info("Received guest user creation request for device: {}", maskDeviceId(request.getDeviceId()));
        
        GuestUserResponse response = guestUserService.createGuestUser(request);
        
        if (response.isSuccess()) {
            log.info("Guest user created successfully for device: {}", maskDeviceId(request.getDeviceId()));
            return ResponseEntity.ok(response);
        } else {
            log.warn("Guest user creation failed for device: {} - {}", maskDeviceId(request.getDeviceId()), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Gets guest session information.
     * 
     * @param sessionToken the session token from Authorization header
     * @return guest session response
     */
    @GetMapping("/session")
    @Operation(
        summary = "Get guest session information",
        description = "Retrieves guest session information including status, expiry, and limitations"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Guest session information retrieved successfully",
        content = @Content(schema = @Schema(implementation = GuestSessionResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Invalid or expired session token",
        content = @Content(schema = @Schema(implementation = GuestSessionResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = GuestSessionResponse.class))
    )
    public ResponseEntity<GuestSessionResponse> getGuestSession(
            @RequestHeader("Authorization") String sessionToken) {
        
        // Extract token from "Bearer <token>" format
        String token = extractToken(sessionToken);
        log.info("Getting guest session info for token: {}", maskSessionToken(token));
        
        GuestSessionResponse response = guestUserService.getGuestSession(token);
        
        if (response.isSuccess()) {
            log.info("Guest session info retrieved successfully");
            return ResponseEntity.ok(response);
        } else {
            log.warn("Failed to get guest session info - {}", response.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Records a guest user action.
     * 
     * @param sessionToken the session token from Authorization header
     * @return action recording response
     */
    @PostMapping("/action")
    @Operation(
        summary = "Record guest user action",
        description = "Records a guest user action and checks if conversion prompt should be shown"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Action recorded successfully",
        content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Invalid session token"
    )
    public ResponseEntity<Object> recordGuestAction(@RequestHeader("Authorization") String sessionToken) {
        String token = extractToken(sessionToken);
        log.debug("Recording guest action for session: {}", maskSessionToken(token));
        
        boolean shouldShowPrompt = guestUserService.recordGuestAction(token);
        
        return ResponseEntity.ok(new Object() {
            public final boolean success = true;
            public final String message = "Action recorded successfully";
            public final boolean shouldShowConversionPrompt = shouldShowPrompt;
        });
    }

    /**
     * Records that a conversion prompt was shown.
     * 
     * @param sessionToken the session token from Authorization header
     * @return conversion prompt recording response
     */
    @PostMapping("/conversion-prompt-shown")
    @Operation(
        summary = "Record conversion prompt shown",
        description = "Records that a conversion prompt was shown to the guest user"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Conversion prompt recorded successfully"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Invalid session token"
    )
    public ResponseEntity<Object> recordConversionPromptShown(@RequestHeader("Authorization") String sessionToken) {
        String token = extractToken(sessionToken);
        log.debug("Recording conversion prompt shown for session: {}", maskSessionToken(token));
        
        boolean recorded = guestUserService.recordConversionPromptShown(token);
        
        return ResponseEntity.ok(new Object() {
            public final boolean success = recorded;
            public final String message = recorded ? "Conversion prompt recorded" : "Failed to record conversion prompt";
        });
    }

    /**
     * Health check endpoint for guest user service.
     * 
     * @return health status
     */
    @GetMapping("/health")
    @Operation(
        summary = "Guest user service health check",
        description = "Check the health status of the guest user service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Guest user service is healthy"
    )
    public ResponseEntity<String> health() {
        log.debug("Guest user service health check requested");
        return ResponseEntity.ok("Guest user service is healthy");
    }

    /**
     * Extracts token from Authorization header.
     * 
     * @param authorizationHeader the Authorization header value
     * @return extracted token
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return "";
        }
        return authorizationHeader.substring(7);
    }

    /**
     * Masks device ID for logging (privacy protection).
     * 
     * @param deviceId device ID to mask
     * @return masked device ID
     */
    private String maskDeviceId(String deviceId) {
        if (deviceId == null || deviceId.length() < 8) {
            return "***";
        }
        return deviceId.substring(0, 4) + "***" + deviceId.substring(deviceId.length() - 4);
    }

    /**
     * Masks session token for logging (privacy protection).
     * 
     * @param sessionToken session token to mask
     * @return masked session token
     */
    private String maskSessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.length() < 8) {
            return "***";
        }
        return sessionToken.substring(0, 4) + "***" + sessionToken.substring(sessionToken.length() - 4);
    }
} 