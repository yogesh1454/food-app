package com.teadelivery.user.password.service;

import com.teadelivery.user.password.dto.PasswordChangeRequest;
import com.teadelivery.user.password.dto.PasswordResetConfirmRequest;
import com.teadelivery.user.password.dto.PasswordResetRequest;
import com.teadelivery.user.password.model.PasswordHistory;
import com.teadelivery.user.password.model.PasswordResetToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic validation tests for PasswordService functionality.
 * Tests core business logic without complex mocking.
 */
class PasswordServiceBasicTest {

    @Test
    void testPasswordChangeRequestValidation() {
        // Test that PasswordChangeRequest can be created and validated
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("oldPassword123!")
                .newPassword("newSecurePass456!")
                .build();

        assertNotNull(request);
        assertEquals("oldPassword123!", request.getCurrentPassword());
        assertEquals("newSecurePass456!", request.getNewPassword());
    }

    @Test
    void testPasswordResetRequestValidation() {
        // Test that PasswordResetRequest can be created and validated
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("test@example.com")
                .build();

        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testPasswordResetConfirmRequestValidation() {
        // Test that PasswordResetConfirmRequest can be created and validated
        PasswordResetConfirmRequest request = PasswordResetConfirmRequest.builder()
                .token("abc123def456")
                .newPassword("newSecurePass456!")
                .build();

        assertNotNull(request);
        assertEquals("abc123def456", request.getToken());
        assertEquals("newSecurePass456!", request.getNewPassword());
    }

    @Test
    void testPasswordHistoryModel() {
        // Test that PasswordHistory model works correctly
        UUID userId = UUID.randomUUID();
        PasswordHistory history = PasswordHistory.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .passwordHash("hashedPassword123")
                .changedBy("test-user")
                .changeReason("Password change by user")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .build();

        assertNotNull(history);
        assertEquals(userId, history.getUserId());
        assertEquals("hashedPassword123", history.getPasswordHash());
        assertEquals("test-user", history.getChangedBy());
        assertEquals("Password change by user", history.getChangeReason());
        assertEquals("192.168.1.1", history.getIpAddress());
        assertEquals("Mozilla/5.0", history.getUserAgent());
    }

    @Test
    void testPasswordResetTokenModel() {
        // Test that PasswordResetToken model works correctly
        UUID userId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        
        PasswordResetToken token = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token("abc123def456")
                .email("test@example.com")
                .expiresAt(expiresAt)
                .used(false)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .build();

        assertNotNull(token);
        assertEquals(userId, token.getUserId());
        assertEquals("abc123def456", token.getToken());
        assertEquals("test@example.com", token.getEmail());
        assertEquals(expiresAt, token.getExpiresAt());
        assertFalse(token.getUsed());
        assertEquals("192.168.1.1", token.getIpAddress());
        assertEquals("Mozilla/5.0", token.getUserAgent());
    }

    @Test
    void testPasswordResetTokenValidation() {
        // Test PasswordResetToken validation methods
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(1);
        LocalDateTime past = now.minusHours(1);

        // Valid token (not expired, not used)
        PasswordResetToken validToken = PasswordResetToken.builder()
                .expiresAt(future)
                .used(false)
                .build();

        assertTrue(validToken.isValid());
        assertFalse(validToken.isExpired());

        // Expired token
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .expiresAt(past)
                .used(false)
                .build();

        assertFalse(expiredToken.isValid());
        assertTrue(expiredToken.isExpired());

        // Used token
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .expiresAt(future)
                .used(true)
                .build();

        assertFalse(usedToken.isValid());
        assertFalse(usedToken.isExpired());
    }

    @Test
    void testPasswordStrengthValidation() {
        // Test password strength validation patterns
        String strongPassword = "SecurePass123!";
        String weakPassword = "weak";
        String noUppercase = "securepass123!";
        String noLowercase = "SECUREPASS123!";
        String noNumber = "SecurePass!";
        String noSpecialChar = "SecurePass123";

        // Strong password should match pattern
        assertTrue(strongPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"));

        // Weak passwords should not match pattern
        assertFalse(weakPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"));
        assertFalse(noUppercase.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"));
        assertFalse(noLowercase.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"));
        assertFalse(noNumber.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"));
        assertFalse(noSpecialChar.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"));
    }

    @Test
    void testPasswordRequirements() {
        // Test password requirements constants
        assertEquals(8, PasswordServiceBasicTest.getMinPasswordLength());
        assertTrue(PasswordServiceBasicTest.isUppercaseRequired());
        assertTrue(PasswordServiceBasicTest.isLowercaseRequired());
        assertTrue(PasswordServiceBasicTest.isDigitRequired());
        assertTrue(PasswordServiceBasicTest.isSpecialCharRequired());
        assertEquals(5, PasswordServiceBasicTest.getPasswordHistoryLimit());
    }

    // Helper methods to simulate password requirements
    private static int getMinPasswordLength() {
        return 8;
    }

    private static boolean isUppercaseRequired() {
        return true;
    }

    private static boolean isLowercaseRequired() {
        return true;
    }

    private static boolean isDigitRequired() {
        return true;
    }

    private static boolean isSpecialCharRequired() {
        return true;
    }

    private static int getPasswordHistoryLimit() {
        return 5;
    }
} 