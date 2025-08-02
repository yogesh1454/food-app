package com.teadelivery.user.password.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Encode password using BCrypt
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        log.debug("Encoding password for user");
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verify if raw password matches encoded password
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Validate password strength
     */
    public PasswordValidationResult validatePasswordStrength(String password) {
        if (password == null) {
            return new PasswordValidationResult(false, "Password cannot be null");
        }

        // Check length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new PasswordValidationResult(false, 
                String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH));
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return new PasswordValidationResult(false, 
                String.format("Password must not exceed %d characters", MAX_PASSWORD_LENGTH));
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            return new PasswordValidationResult(false, 
                "Password must contain at least one uppercase letter");
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            return new PasswordValidationResult(false, 
                "Password must contain at least one lowercase letter");
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            return new PasswordValidationResult(false, 
                "Password must contain at least one digit");
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            return new PasswordValidationResult(false, 
                "Password must contain at least one special character");
        }

        return new PasswordValidationResult(true, "Password is strong");
    }

    /**
     * Check if password is considered weak (common passwords)
     */
    public boolean isWeakPassword(String password) {
        if (password == null) {
            return true;
        }

        String lowerPassword = password.toLowerCase();
        
        // Common weak passwords
        String[] weakPasswords = {
            "password", "123456", "password123", "admin", "qwerty",
            "letmein", "welcome", "monkey", "dragon", "master",
            "123456789", "12345678", "12345", "1234567890"
        };

        for (String weak : weakPasswords) {
            if (lowerPassword.equals(weak)) {
                return true;
            }
        }

        // Check for sequential patterns
        if (hasSequentialPattern(password)) {
            return true;
        }

        return false;
    }

    /**
     * Check for sequential patterns in password
     */
    private boolean hasSequentialPattern(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Check for sequential letters (abc, xyz)
        for (int i = 0; i < lowerPassword.length() - 2; i++) {
            char c1 = lowerPassword.charAt(i);
            char c2 = lowerPassword.charAt(i + 1);
            char c3 = lowerPassword.charAt(i + 2);
            
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
        }
        
        // Check for sequential numbers (123, 789)
        for (int i = 0; i < password.length() - 2; i++) {
            if (Character.isDigit(password.charAt(i)) && 
                Character.isDigit(password.charAt(i + 1)) && 
                Character.isDigit(password.charAt(i + 2))) {
                
                int n1 = Character.getNumericValue(password.charAt(i));
                int n2 = Character.getNumericValue(password.charAt(i + 1));
                int n3 = Character.getNumericValue(password.charAt(i + 2));
                
                if (n2 == n1 + 1 && n3 == n2 + 1) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Password validation result
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;

        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
