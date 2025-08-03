package com.teadelivery.user.profile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for phone number validation and normalization.
 * Follows coding standards with proper logging and documentation.
 */
@Slf4j
@Service
public class PhoneNumberValidator {

    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    private static final Pattern INDIAN_PHONE_PATTERN = Pattern.compile("^\\+91[6-9]\\d{9}$");

    /**
     * Validates if the phone number is in correct international format.
     * 
     * @param phoneNumber phone number to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String normalized = normalizePhoneNumber(phoneNumber);
        return INTERNATIONAL_PHONE_PATTERN.matcher(normalized).matches();
    }

    /**
     * Normalizes phone number to international format.
     * 
     * @param phoneNumber phone number to normalize
     * @return normalized phone number
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");
        
        // Handle Indian numbers without country code
        if (cleaned.startsWith("91") && cleaned.length() == 12) {
            return "+" + cleaned;
        }
        
        // Handle Indian numbers starting with 6-9
        if (cleaned.matches("^[6-9]\\d{9}$")) {
            return "+91" + cleaned;
        }
        
        // Handle numbers starting with 0 (remove leading 0)
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }
        
        // Add + if not present
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }
        
        return cleaned;
    }

    /**
     * Checks if the phone number is an Indian number.
     * 
     * @param phoneNumber phone number to check
     * @return true if Indian number, false otherwise
     */
    public boolean isIndianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        
        String normalized = normalizePhoneNumber(phoneNumber);
        return INDIAN_PHONE_PATTERN.matcher(normalized).matches();
    }
} 