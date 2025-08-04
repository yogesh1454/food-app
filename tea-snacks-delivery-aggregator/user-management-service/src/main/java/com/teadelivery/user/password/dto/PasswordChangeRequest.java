package com.teadelivery.user.password.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password change requests.
 * Follows coding standards with comprehensive validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password change request")
public class PasswordChangeRequest {
    
    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password", example = "currentPassword123", required = true)
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(
        description = "New password (must be at least 8 characters with uppercase, lowercase, number, and special character)", 
        example = "NewSecurePass123!", 
        required = true
    )
    private String newPassword;
} 