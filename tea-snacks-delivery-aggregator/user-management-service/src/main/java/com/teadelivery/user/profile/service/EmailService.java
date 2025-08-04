package com.teadelivery.user.profile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for email operations.
 * Follows coding standards with comprehensive email handling.
 */
@Slf4j
@Service
public class EmailService {

    /**
     * Send verification email.
     * 
     * @param email recipient email
     * @param verificationToken verification token
     * @return true if sent successfully, false otherwise
     */
    public boolean sendVerificationEmail(String email, String verificationToken) {
        log.info("Sending verification email to: {}", maskEmail(email));
        
        try {
            // In a real implementation, you would use an email service like SendGrid
            // For now, we'll simulate the email sending
            String subject = "Verify Your Email Address";
            String body = buildVerificationEmailBody(email, verificationToken);
            
            // Simulate email sending
            log.info("Verification email content for {}: Subject: {}, Body: {}", 
                    maskEmail(email), subject, body.substring(0, Math.min(100, body.length())) + "...");
            
            // Simulate network delay
            Thread.sleep(100);
            
            log.info("Verification email sent successfully to: {}", maskEmail(email));
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", maskEmail(email), e);
            return false;
        }
    }

    /**
     * Send profile update notification email.
     * 
     * @param email recipient email
     * @param updateDetails update details
     * @return true if sent successfully, false otherwise
     */
    public boolean sendProfileUpdateNotification(String email, String updateDetails) {
        log.info("Sending profile update notification to: {}", maskEmail(email));
        
        try {
            String subject = "Profile Updated Successfully";
            String body = buildProfileUpdateEmailBody(email, updateDetails);
            
            // Simulate email sending
            log.info("Profile update notification sent successfully to: {}", maskEmail(email));
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send profile update notification to: {}", maskEmail(email), e);
            return false;
        }
    }

    /**
     * Build verification email body.
     * 
     * @param email recipient email
     * @param verificationToken verification token
     * @return email body
     */
    private String buildVerificationEmailBody(String email, String verificationToken) {
        return String.format("""
            Hello,
            
            You have requested to update your email address to: %s
            
            Please click the following link to verify your email address:
            https://app.teasnacks.com/verify-email?token=%s&email=%s
            
            If you did not request this change, please ignore this email.
            
            This verification link will expire in 24 hours.
            
            Best regards,
            Tea & Snacks Team
            """, email, verificationToken, email);
    }

    /**
     * Build profile update email body.
     * 
     * @param email recipient email
     * @param updateDetails update details
     * @return email body
     */
    private String buildProfileUpdateEmailBody(String email, String updateDetails) {
        return String.format("""
            Hello,
            
            Your profile has been updated successfully.
            
            Update details: %s
            
            If you did not make this change, please contact our support team immediately.
            
            Best regards,
            Tea & Snacks Team
            """, updateDetails);
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