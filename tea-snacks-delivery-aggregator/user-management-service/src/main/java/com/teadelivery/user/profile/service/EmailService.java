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
     * Send password reset email.
     * 
     * @param email recipient email
     * @param resetToken reset token
     * @return true if sent successfully, false otherwise
     */
    public boolean sendPasswordResetEmail(String email, String resetToken) {
        log.info("Sending password reset email to: {}", maskEmail(email));
        
        try {
            String subject = "Password Reset Request";
            String body = buildPasswordResetEmailBody(email, resetToken);
            
            // Simulate email sending
            log.info("Password reset email content for {}: Subject: {}, Body: {}", 
                    maskEmail(email), subject, body.substring(0, Math.min(100, body.length())) + "...");
            
            // Simulate network delay
            Thread.sleep(100);
            
            log.info("Password reset email sent successfully to: {}", maskEmail(email));
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", maskEmail(email), e);
            return false;
        }
    }

    /**
     * Send password change notification email.
     * 
     * @param email recipient email
     * @return true if sent successfully, false otherwise
     */
    public boolean sendPasswordChangeNotification(String email) {
        log.info("Sending password change notification to: {}", maskEmail(email));
        
        try {
            String subject = "Password Changed Successfully";
            String body = buildPasswordChangeEmailBody(email);
            
            // Simulate email sending
            log.info("Password change notification sent successfully to: {}", maskEmail(email));
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send password change notification to: {}", maskEmail(email), e);
            return false;
        }
    }

    /**
     * Send password reset notification email.
     * 
     * @param email recipient email
     * @return true if sent successfully, false otherwise
     */
    public boolean sendPasswordResetNotification(String email) {
        log.info("Sending password reset notification to: {}", maskEmail(email));
        
        try {
            String subject = "Password Reset Completed";
            String body = buildPasswordResetNotificationEmailBody(email);
            
            // Simulate email sending
            log.info("Password reset notification sent successfully to: {}", maskEmail(email));
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send password reset notification to: {}", maskEmail(email), e);
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
     * Build password reset email body.
     * 
     * @param email recipient email
     * @param resetToken reset token
     * @return email body
     */
    private String buildPasswordResetEmailBody(String email, String resetToken) {
        return String.format("""
            Hello,
            
            You have requested to reset your password for your Tea & Snacks account.
            
            Please click the following link to reset your password:
            https://app.teasnacks.com/reset-password?token=%s&email=%s
            
            This link will expire in 24 hours.
            
            If you did not request this password reset, please ignore this email.
            
            Best regards,
            Tea & Snacks Team
            """, resetToken, email);
    }

    /**
     * Build password change notification email body.
     * 
     * @param email recipient email
     * @return email body
     */
    private String buildPasswordChangeEmailBody(String email) {
        return String.format("""
            Hello,
            
            Your password has been changed successfully.
            
            If you did not make this change, please contact our support team immediately.
            
            Best regards,
            Tea & Snacks Team
            """);
    }

    /**
     * Build password reset notification email body.
     * 
     * @param email recipient email
     * @return email body
     */
    private String buildPasswordResetNotificationEmailBody(String email) {
        return String.format("""
            Hello,
            
            Your password has been reset successfully.
            
            If you did not make this change, please contact our support team immediately.
            
            Best regards,
            Tea & Snacks Team
            """);
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