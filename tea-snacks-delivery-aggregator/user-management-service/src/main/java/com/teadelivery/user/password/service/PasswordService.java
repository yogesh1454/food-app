package com.teadelivery.user.password.service;

import com.teadelivery.user.password.dto.PasswordChangeRequest;
import com.teadelivery.user.password.dto.PasswordResetConfirmRequest;
import com.teadelivery.user.password.dto.PasswordResetRequest;
import com.teadelivery.user.password.model.PasswordHistory;
import com.teadelivery.user.password.model.PasswordResetToken;
import com.teadelivery.user.password.repository.PasswordHistoryRepository;
import com.teadelivery.user.password.repository.PasswordResetTokenRepository;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.repository.UserRepository;
import com.teadelivery.user.profile.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for password management operations.
 * Follows coding standards with comprehensive security features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Configuration constants
    private static final int PASSWORD_HISTORY_LIMIT = 5;
    private static final int RESET_TOKEN_EXPIRY_HOURS = 24;
    private static final int MAX_RESET_ATTEMPTS_PER_HOUR = 3;

    /**
     * Change password for authenticated user.
     * 
     * @param userId user ID
     * @param request password change request
     * @return change result
     */
    @Transactional
    public Map<String, Object> changePassword(UUID userId, PasswordChangeRequest request) {
        log.info("Changing password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        validateNewPassword(userId, request.getNewPassword());
        
        // Update password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
        
        // Record password history
        recordPasswordHistory(user, "Password changed by user", "PASSWORD_CHANGE");
        
        // Invalidate existing sessions (in a real implementation, you would invalidate JWT tokens)
        invalidateUserSessions(userId);
        
        // Send notification email
        sendPasswordChangeNotification(user.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        response.put("password_changed", true);
        
        log.info("Password changed successfully for user: {}", userId);
        return response;
    }

    /**
     * Request password reset.
     * 
     * @param request password reset request
     * @return reset request result
     */
    @Transactional
    public Map<String, Object> requestPasswordReset(PasswordResetRequest request) {
        log.info("Requesting password reset for email: {}", maskEmail(request.getEmail()));
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        // Check rate limiting
        checkResetRateLimit(user.getId());
        
        // Generate reset token
        String resetToken = generateResetToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS);
        
        // Create reset token entity
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .userId(user.getId())
                .token(resetToken)
                .email(user.getEmail())
                .expiresAt(expiresAt)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .build();
        
        passwordResetTokenRepository.save(tokenEntity);
        
        // Send reset email
        boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        
        if (!emailSent) {
            throw new RuntimeException("Failed to send password reset email");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset instructions sent to your email");
        response.put("email_sent", true);
        
        log.info("Password reset requested successfully for user: {}", user.getId());
        return response;
    }

    /**
     * Confirm password reset with token.
     * 
     * @param request password reset confirmation request
     * @return reset confirmation result
     */
    @Transactional
    public Map<String, Object> confirmPasswordReset(PasswordResetConfirmRequest request) {
        log.info("Confirming password reset with token");
        
        // Find valid token
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate new password
        validateNewPassword(user.getId(), request.getNewPassword());
        
        // Update password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
        
        // Mark token as used
        passwordResetTokenRepository.markTokenAsUsed(request.getToken());
        
        // Record password history
        recordPasswordHistory(user, "Password reset via email", "PASSWORD_RESET");
        
        // Invalidate existing sessions
        invalidateUserSessions(user.getId());
        
        // Send notification email
        sendPasswordResetNotification(user.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        response.put("password_reset", true);
        
        log.info("Password reset confirmed successfully for user: {}", user.getId());
        return response;
    }

    /**
     * Validate new password against requirements.
     * 
     * @param userId user ID
     * @param newPassword new password
     */
    private void validateNewPassword(UUID userId, String newPassword) {
        // Check password strength (already validated by DTO)
        
        // Check password history
        String newPasswordHash = passwordEncoder.encode(newPassword);
        if (passwordHistoryRepository.existsByUserIdAndPasswordHash(userId, newPasswordHash)) {
            throw new RuntimeException("Password cannot be the same as any of your last 5 passwords");
        }
        
        // Check for common patterns
        validatePasswordPatterns(newPassword);
    }

    /**
     * Validate password patterns.
     * 
     * @param password password to validate
     */
    private void validatePasswordPatterns(String password) {
        // Check for common dictionary words
        String[] commonWords = {"password", "123456", "qwerty", "admin", "user"};
        String lowerPassword = password.toLowerCase();
        
        for (String word : commonWords) {
            if (lowerPassword.contains(word)) {
                throw new RuntimeException("Password contains common words that are not allowed");
            }
        }
        
        // Check for sequential characters
        if (hasSequentialCharacters(password)) {
            throw new RuntimeException("Password contains sequential characters");
        }
    }

    /**
     * Check for sequential characters.
     * 
     * @param password password to check
     * @return true if sequential characters found
     */
    private boolean hasSequentialCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if (Character.isDigit(c1) && Character.isDigit(c2) && Character.isDigit(c3)) {
                if (c2 == c1 + 1 && c3 == c2 + 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check reset rate limit.
     * 
     * @param userId user ID
     */
    private void checkResetRateLimit(UUID userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = passwordResetTokenRepository
                .countByUserIdAndUsedFalseAndExpiresAtAfter(userId, oneHourAgo);
        
        if (recentAttempts >= MAX_RESET_ATTEMPTS_PER_HOUR) {
            throw new RuntimeException("Too many password reset attempts. Please try again later.");
        }
    }

    /**
     * Record password history.
     * 
     * @param user user entity
     * @param changeReason change reason
     * @param changeType change type
     */
    private void recordPasswordHistory(User user, String changeReason, String changeType) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String changedBy = authentication != null ? authentication.getName() : "system";
            
                    PasswordHistory history = PasswordHistory.builder()
                .userId(user.getId())
                .passwordHash(user.getPasswordHash())
                .changedBy(changedBy)
                .changeReason(changeReason)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .build();
            
            passwordHistoryRepository.save(history);
            
            // Keep only last 5 password entries
            List<PasswordHistory> recentHistory = passwordHistoryRepository
                    .findByUserIdOrderByCreatedAtDesc(user.getId());
            
            if (recentHistory.size() > PASSWORD_HISTORY_LIMIT) {
                for (int i = PASSWORD_HISTORY_LIMIT; i < recentHistory.size(); i++) {
                    passwordHistoryRepository.delete(recentHistory.get(i));
                }
            }
            
            log.debug("Password history recorded for user: {}", user.getId());
            
        } catch (Exception e) {
            log.error("Failed to record password history for user: {}", user.getId(), e);
        }
    }

    /**
     * Generate reset token.
     * 
     * @return reset token
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * Invalidate user sessions.
     * 
     * @param userId user ID
     */
    private void invalidateUserSessions(UUID userId) {
        // In a real implementation, you would:
        // 1. Add user's tokens to a blacklist
        // 2. Notify other services about session invalidation
        // 3. Update user's last password change timestamp
        
        log.info("Invalidating sessions for user: {}", userId);
    }

    /**
     * Send password change notification.
     * 
     * @param email user email
     */
    private void sendPasswordChangeNotification(String email) {
        try {
            emailService.sendPasswordChangeNotification(email);
            log.info("Password change notification sent to: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Failed to send password change notification to: {}", maskEmail(email), e);
        }
    }

    /**
     * Send password reset notification.
     * 
     * @param email user email
     */
    private void sendPasswordResetNotification(String email) {
        try {
            emailService.sendPasswordResetNotification(email);
            log.info("Password reset notification sent to: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Failed to send password reset notification to: {}", maskEmail(email), e);
        }
    }

    /**
     * Get client IP address.
     * 
     * @return client IP address
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP address", e);
        }
        return "unknown";
    }

    /**
     * Get user agent.
     * 
     * @return user agent
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Failed to get user agent", e);
        }
        return "unknown";
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
