package com.teadelivery.user.auth.controller;

import com.teadelivery.user.auth.dto.LoginRequest;
import com.teadelivery.user.auth.dto.LoginResponse;
import com.teadelivery.user.auth.dto.RefreshTokenRequest;
import com.teadelivery.user.auth.dto.RefreshTokenResponse;
import com.teadelivery.user.auth.dto.UserRegistrationRequest;
import com.teadelivery.user.auth.dto.UserRegistrationResponse;
import com.teadelivery.user.auth.dto.GuestConversionRequest;
import com.teadelivery.user.auth.service.AuthenticationService;
import com.teadelivery.user.integration.email.NotificationClient;
import com.teadelivery.user.profile.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for login, logout, and token management.
 * Follows coding standards with comprehensive API documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final NotificationClient notificationClient;

    /**
     * Authenticates user and returns JWT tokens.
     * 
     * @param request login request
     * @return login response with tokens
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", maskUsername(request.getUsername()));
        
        try {
            LoginResponse response = authenticationService.login(request);
            log.info("Login successful for user: {}", response.getUserId());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for username: {} - {}", maskUsername(request.getUsername()), e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Login error for username: {}", maskUsername(request.getUsername()), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Refreshes access token using refresh token.
     * 
     * @param request refresh token request
     * @return refresh token response
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refreshes access token using refresh token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        
        try {
            RefreshTokenResponse response = authenticationService.refreshToken(request);
            log.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Logs out user by invalidating tokens.
     * 
     * @param authorization authorization header with Bearer token
     * @return logout response
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logs out user by invalidating tokens")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorization) {
        log.info("Logout request received");
        
        try {
            // Extract token from Authorization header
            String token = extractTokenFromHeader(authorization);
            String response = authenticationService.logout(token);
            log.info("Logout successful");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Health check endpoint for authentication service.
     * 
     * @return health status
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Health check for authentication service")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is healthy");
    }

    /**
     * Registers a new user with email and password.
     * 
     * @param request registration request
     * @return registration response with tokens
     */
    @PostMapping("/register/email")
    @Operation(summary = "Email registration", description = "Registers a new user with email and password")
    @ApiResponse(
        responseCode = "200",
        description = "User registered successfully",
        content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data or user already exists",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<UserRegistrationResponse> registerWithEmail(@RequestBody UserRegistrationRequest request) {
        log.info("Email registration request received for email: {}", maskEmail(request.getEmail()));
        
        try {
            User user = authenticationService.createUserFromEmailRegistration(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName(),
                    request.getPhoneNumber()
            );
            
            // Generate tokens for the new user
            String accessToken = authenticationService.generateTokensForUser(user);
            String refreshToken = authenticationService.generateRefreshTokenForUser(user);
            
            UserRegistrationResponse response = UserRegistrationResponse.builder()
                    .userId(user.getId().toString())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .username(user.getEmail())
                    .role(user.getRole().name())
                    .userType(user.getUserType().name())
                    .profileCompletion(user.getProfileCompletionPercentage())
                    .message("User registered successfully")
                    .build();
        
            // Send registration verification email asynchronously
            try {
                notificationClient.sendRegistrationVerificationEmail(
                    user.getId(),
                    user.getEmail(),
                    user.getName() != null ? user.getName() : "User",
                    "123456", // TODO: Generate actual verification code
                    accessToken // Using access token as verification token for now
                );
                log.info("Registration verification email sent for user: {}", user.getId());
            } catch (Exception e) {
                log.warn("Failed to send registration verification email for user: {} - {}", user.getId(), e.getMessage());
                // Don't fail registration if email sending fails
            }
        
            log.info("Email registration successful for user: {}", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Email registration failed for email: {}", maskEmail(request.getEmail()), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Converts a guest user to a registered user.
     * 
     * @param request guest conversion request
     * @return registration response with tokens
     */
    @PostMapping("/guest/convert")
    @Operation(summary = "Guest conversion", description = "Converts a guest user to a registered user")
    @ApiResponse(
        responseCode = "200",
        description = "Guest user converted successfully",
        content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request data or user already exists",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<UserRegistrationResponse> convertGuestToUser(@RequestBody GuestConversionRequest request) {
        log.info("Guest conversion request received for guest user: {}", request.getGuestUserId());
        
        try {
            // Split name into first and last name
            String[] nameParts = request.getName().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            User user = authenticationService.convertGuestToRegisteredUser(
                    java.util.UUID.fromString(request.getGuestUserId()),
                    request.getEmail() != null ? request.getEmail() : request.getPhoneNumber(),
                    request.getPassword(),
                    firstName,
                    lastName,
                    request.getEmail(),
                    request.getPhoneNumber()
            );
            
            // Generate tokens for the converted user
            String accessToken = authenticationService.generateTokensForUser(user);
            String refreshToken = authenticationService.generateRefreshTokenForUser(user);
            
            UserRegistrationResponse response = UserRegistrationResponse.builder()
                    .userId(user.getId().toString())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .username(user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                    .role(user.getRole().name())
                    .userType(user.getUserType().name())
                    .profileCompletion(user.getProfileCompletionPercentage())
                    .message("Guest user converted successfully")
                    .build();
            
            log.info("Guest conversion successful for user: {}", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Guest conversion failed for guest user: {}", request.getGuestUserId(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Extracts token from Authorization header.
     * 
     * @param authorization authorization header
     * @return extracted token
     */
    private String extractTokenFromHeader(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid authorization header");
        }
        return authorization.substring(7);
    }

    /**
     * Masks username for logging (privacy protection).
     * 
     * @param username username to mask
     * @return masked username
     */
    private String maskUsername(String username) {
        if (username == null || username.length() < 3) {
            return "***";
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 1);
    }

    /**
     * Masks email for logging (privacy protection).
     * 
     * @param email email to mask
     * @return masked email
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 5) {
            return "***";
        }
        return email.substring(0, 2) + "***" + email.substring(email.length() - 2);
    }
} 