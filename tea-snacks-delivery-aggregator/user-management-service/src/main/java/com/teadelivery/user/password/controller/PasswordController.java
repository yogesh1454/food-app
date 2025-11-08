package com.teadelivery.user.password.controller;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.password.dto.PasswordChangeRequest;
import com.teadelivery.user.password.dto.PasswordResetConfirmRequest;
import com.teadelivery.user.password.dto.PasswordResetRequest;
import com.teadelivery.user.password.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for password management operations.
 * Follows coding standards with comprehensive security features.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password Management", description = "APIs for password management and reset")
public class PasswordController {

    private final PasswordService passwordService;

    /**
     * Change password for authenticated user.
     * 
     * @param request password change request
     * @return change result
     */
    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Changes password for authenticated user")
    @ApiResponse(
        responseCode = "200",
        description = "Password changed successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid current password or new password requirements not met",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received for current user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        
        Map<String, Object> response = passwordService.changePassword(userId, request);
        
        log.info("Password changed successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Change password for specific user (admin function).
     * 
     * @param userId user ID
     * @param request password change request
     * @return change result
     */
    @PutMapping("/{userId}/password")
    @HasPermission(resource = "password", action = "manage")
    @Operation(summary = "Change password for user", description = "Changes password for specific user (admin function)")
    @ApiResponse(
        responseCode = "200",
        description = "Password changed successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid current password or new password requirements not met",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Access denied",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> changeUserPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received for user: {}", userId);
        
        Map<String, Object> response = passwordService.changePassword(userId, request);
        
        log.info("Password changed successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset.
     * 
     * @param request password reset request
     * @return reset request result
     */
    @PostMapping("/password-reset/request")
    @Operation(summary = "Request password reset", description = "Requests password reset via email")
    @ApiResponse(
        responseCode = "200",
        description = "Password reset instructions sent to email",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid email or user not found",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "429",
        description = "Too many reset attempts",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request received for email: {}", maskEmail(request.getEmail()));
        
        Map<String, Object> response = passwordService.requestPasswordReset(request);
        
        log.info("Password reset requested successfully for email: {}", maskEmail(request.getEmail()));
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm password reset with token.
     * 
     * @param request password reset confirmation request
     * @return reset confirmation result
     */
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset", description = "Confirms password reset with token")
    @ApiResponse(
        responseCode = "200",
        description = "Password reset successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid or expired token",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<Map<String, Object>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        log.info("Password reset confirmation request received");
        
        Map<String, Object> response = passwordService.confirmPasswordReset(request);
        
        log.info("Password reset confirmed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get password strength requirements.
     * 
     * @return password requirements
     */
    @GetMapping("/password/requirements")
    @Operation(summary = "Get password requirements", description = "Returns password strength requirements")
    @ApiResponse(
        responseCode = "200",
        description = "Password requirements retrieved successfully",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    public ResponseEntity<Map<String, Object>> getPasswordRequirements() {
        log.info("Password requirements request received");
        
        Map<String, Object> requirements = Map.of(
            "min_length", 8,
            "require_uppercase", true,
            "require_lowercase", true,
            "require_digit", true,
            "require_special_char", true,
            "max_history", 5,
            "description", "Password must be at least 8 characters with uppercase, lowercase, number, and special character"
        );
        
        log.info("Password requirements retrieved successfully");
        return ResponseEntity.ok(requirements);
    }

    /**
     * Mask email for privacy.
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