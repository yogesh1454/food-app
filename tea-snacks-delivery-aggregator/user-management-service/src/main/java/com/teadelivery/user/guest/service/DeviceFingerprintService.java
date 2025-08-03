package com.teadelivery.user.guest.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for device fingerprinting and validation.
 * Follows coding standards with proper validation and logging.
 */
@Slf4j
@Service
public class DeviceFingerprintService {

    // Pattern for valid device IDs (alphanumeric with hyphens and underscores)
    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{8,64}$");
    
    // Minimum length for device ID
    private static final int MIN_DEVICE_ID_LENGTH = 8;
    
    // Maximum length for device ID
    private static final int MAX_DEVICE_ID_LENGTH = 64;

    /**
     * Validates if a device ID is valid.
     * 
     * @param deviceId the device ID to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.debug("Device ID is null or empty");
            return false;
        }
        
        if (deviceId.length() < MIN_DEVICE_ID_LENGTH || deviceId.length() > MAX_DEVICE_ID_LENGTH) {
            log.debug("Device ID length invalid: {} (length: {})", maskDeviceId(deviceId), deviceId.length());
            return false;
        }
        
        if (!DEVICE_ID_PATTERN.matcher(deviceId).matches()) {
            log.debug("Device ID format invalid: {}", maskDeviceId(deviceId));
            return false;
        }
        
        // Check for common invalid patterns
        if (isCommonInvalidPattern(deviceId)) {
            log.debug("Device ID contains invalid pattern: {}", maskDeviceId(deviceId));
            return false;
        }
        
        log.debug("Device ID validation passed: {}", maskDeviceId(deviceId));
        return true;
    }

    /**
     * Generates a device fingerprint from user agent and IP.
     * 
     * @param userAgent the user agent string
     * @param ipAddress the IP address
     * @return device fingerprint hash
     */
    public String generateDeviceFingerprint(String userAgent, String ipAddress) {
        if (userAgent == null) userAgent = "";
        if (ipAddress == null) ipAddress = "";
        
        // Simple hash generation (in production, use more sophisticated fingerprinting)
        String fingerprint = userAgent + "|" + ipAddress;
        return Integer.toHexString(fingerprint.hashCode());
    }

    /**
     * Checks if device ID contains common invalid patterns.
     * 
     * @param deviceId the device ID to check
     * @return true if invalid pattern found
     */
    private boolean isCommonInvalidPattern(String deviceId) {
        String lowerDeviceId = deviceId.toLowerCase();
        
        // Check for common test or invalid patterns
        return lowerDeviceId.contains("test") ||
               lowerDeviceId.contains("null") ||
               lowerDeviceId.contains("undefined") ||
               lowerDeviceId.contains("000000") ||
               lowerDeviceId.contains("111111") ||
               lowerDeviceId.matches(".*[a-z]{10,}.*") || // Too many consecutive letters
               lowerDeviceId.matches(".*[0-9]{10,}.*");   // Too many consecutive numbers
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
} 