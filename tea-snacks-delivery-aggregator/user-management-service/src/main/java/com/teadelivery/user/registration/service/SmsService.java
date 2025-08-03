package com.teadelivery.user.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS messages.
 * Currently a mock implementation for development.
 * Follows coding standards with proper logging and documentation.
 */
@Slf4j
@Service
public class SmsService {

    /**
     * Sends OTP via SMS to the specified phone number.
     * 
     * @param phoneNumber phone number to send SMS to
     * @param otp OTP code to send
     * @return true if SMS sent successfully, false otherwise
     */
    public boolean sendOtp(String phoneNumber, String otp) {
        try {
            // Mock SMS sending - in production, this would integrate with SMS provider
            log.info("SMS OTP sent to {}: {}", maskPhoneNumber(phoneNumber), otp);
            
            // Simulate SMS delivery success/failure (90% success rate for testing)
            boolean success = Math.random() > 0.1;
            
            if (success) {
                log.info("SMS delivered successfully to: {}", maskPhoneNumber(phoneNumber));
            } else {
                log.warn("SMS delivery failed to: {}", maskPhoneNumber(phoneNumber));
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error sending SMS to: {}", maskPhoneNumber(phoneNumber), e);
            return false;
        }
    }

    /**
     * Masks phone number for logging (privacy protection).
     * 
     * @param phoneNumber phone number to mask
     * @return masked phone number
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "***";
        }
        return phoneNumber.substring(0, 2) + "***" + phoneNumber.substring(phoneNumber.length() - 2);
    }
} 