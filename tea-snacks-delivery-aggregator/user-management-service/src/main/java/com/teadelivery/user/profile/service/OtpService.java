package com.teadelivery.user.profile.service;

import com.teadelivery.user.profile.dto.OtpRequest;
import com.teadelivery.user.profile.dto.OtpResponse;
import com.teadelivery.user.profile.dto.OtpVerificationRequest;
import com.teadelivery.user.profile.dto.OtpVerificationResponse;
import com.teadelivery.user.profile.model.OtpSession;
import com.teadelivery.user.profile.repository.OtpSessionRepository;
import com.teadelivery.user.auth.service.AuthenticationService;
import com.teadelivery.user.profile.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling OTP generation, validation, and session management.
 * Follows coding standards with proper logging, error handling, and immutability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpSessionRepository otpSessionRepository;
    private final PhoneNumberValidator phoneNumberValidator;
    private final SmsService smsService;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_REQUESTS = 3;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 10;
    
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates and sends OTP for phone registration.
     * 
     * @param request OTP request containing phone number
     * @return OTP response with session details
     */
    @Transactional
    public OtpResponse generateAndSendOtp(OtpRequest request) {
        log.info("Generating OTP for phone number: {}", maskPhoneNumber(request.getPhoneNumber()));
        
        try {
            // Validate phone number format
            if (!phoneNumberValidator.isValidPhoneNumber(request.getPhoneNumber())) {
                log.warn("Invalid phone number format: {}", request.getPhoneNumber());
                return OtpResponse.builder()
                    .success(false)
                    .message("Invalid phone number format")
                    .build();
            }

            // Check rate limiting
            if (isRateLimited(request.getPhoneNumber())) {
                log.warn("Rate limit exceeded for phone number: {}", maskPhoneNumber(request.getPhoneNumber()));
                return OtpResponse.builder()
                    .success(false)
                    .message("Too many OTP requests. Please try again later.")
                    .build();
            }

            // Generate OTP
            String otp = generateOtp();
            String sessionId = UUID.randomUUID().toString();
            
            // Create OTP session
            OtpSession otpSession = OtpSession.builder()
                .sessionId(sessionId)
                .phoneNumber(request.getPhoneNumber())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .attemptsRemaining(MAX_VERIFICATION_ATTEMPTS)
                .used(false)
                .build();
            
            otpSessionRepository.save(otpSession);
            
            // Send OTP via SMS
            boolean smsSent = smsService.sendOtp(request.getPhoneNumber(), otp);
            
            if (!smsSent) {
                log.error("Failed to send SMS OTP to: {}", maskPhoneNumber(request.getPhoneNumber()));
                return OtpResponse.builder()
                    .success(false)
                    .message("Failed to send OTP. Please try again.")
                    .build();
            }
            
            log.info("OTP sent successfully for session: {}", sessionId);
            
            return OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully")
                .sessionId(sessionId)
                .expiryMinutes(OTP_EXPIRY_MINUTES)
                .resendAllowed(true)
                .attemptsRemaining((int) (MAX_OTP_REQUESTS - getOtpRequestCount(request.getPhoneNumber())))
                .build();
                
        } catch (Exception e) {
            log.error("Error generating OTP for phone: {}", maskPhoneNumber(request.getPhoneNumber()), e);
            return OtpResponse.builder()
                .success(false)
                .message("Internal server error. Please try again.")
                .build();
        }
    }

    /**
     * Verifies OTP and creates user account.
     * 
     * @param request OTP verification request
     * @return OTP verification response with user details
     */
    @Transactional
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest request) {
        log.info("Verifying OTP for session: {}", request.getSessionId());
        
        try {
            // Find OTP session
            Optional<OtpSession> sessionOpt = otpSessionRepository.findBySessionId(request.getSessionId());
            
            if (sessionOpt.isEmpty()) {
                log.warn("Invalid session ID: {}", request.getSessionId());
                return OtpVerificationResponse.builder()
                    .success(false)
                    .message("Invalid session")
                    .build();
            }
            
            OtpSession session = sessionOpt.get();
            
            // Check if session is expired
            if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
                log.warn("OTP session expired: {}", request.getSessionId());
                return OtpVerificationResponse.builder()
                    .success(false)
                    .message("OTP has expired. Please request a new one.")
                    .build();
            }
            
            // Check if phone number matches
            if (!session.getPhoneNumber().equals(request.getPhoneNumber())) {
                log.warn("Phone number mismatch for session: {}", request.getSessionId());
                return OtpVerificationResponse.builder()
                    .success(false)
                    .message("Invalid phone number")
                    .build();
            }
            
            // Check remaining attempts
            if (session.getAttemptsRemaining() <= 0) {
                log.warn("No attempts remaining for session: {}", request.getSessionId());
                return OtpVerificationResponse.builder()
                    .success(false)
                    .message("Too many failed attempts. Please request a new OTP.")
                    .build();
            }
            
            // Verify OTP
            if (!session.getOtp().equals(request.getOtp())) {
                // Decrement attempts
                session.setAttemptsRemaining(session.getAttemptsRemaining() - 1);
                otpSessionRepository.save(session);
                
                log.warn("Invalid OTP for session: {}. Attempts remaining: {}", 
                    request.getSessionId(), session.getAttemptsRemaining());
                
                return OtpVerificationResponse.builder()
                    .success(false)
                    .message("Invalid OTP")
                    .attemptsRemaining(session.getAttemptsRemaining())
                    .build();
            }
            
            // OTP is valid - create user account
            log.info("OTP verified successfully for session: {}", request.getSessionId());
            
            // Create user account from OTP verification
            String username = request.getPhoneNumber(); // Use phone number as username
            String password = passwordEncoder.encode("tempPassword123"); // Temporary password
            String firstName = request.getName();
            String lastName = ""; // Can be added later
            String email = request.getEmail();
            String phoneNumber = request.getPhoneNumber();
            
            try {
                authenticationService.createUserFromOtpVerification(
                    username, password, firstName, lastName, email, phoneNumber
                );
                
                // Mark session as used
                session.setUsed(true);
                session.setUsedAt(LocalDateTime.now());
                otpSessionRepository.save(session);
                
                return OtpVerificationResponse.builder()
                    .success(true)
                    .message("OTP verified successfully. User account created.")
                    .build();
                    
            } catch (Exception e) {
                log.error("Error creating user account from OTP verification: {}", request.getSessionId(), e);
                return OtpVerificationResponse.builder()
                    .success(false)
                    .message("OTP verification successful but user account creation failed. Please try again.")
                    .build();
            }
                
        } catch (Exception e) {
            log.error("Error verifying OTP for session: {}", request.getSessionId(), e);
            return OtpVerificationResponse.builder()
                .success(false)
                .message("Internal server error. Please try again.")
                .build();
        }
    }

    /**
     * Resends OTP for existing session.
     * 
     * @param request Resend OTP request
     * @return OTP response with updated session details
     */
    @Transactional
    public OtpResponse resendOtp(OtpRequest request) {
        log.info("Resending OTP for phone number: {}", maskPhoneNumber(request.getPhoneNumber()));
        
        try {
            // Check rate limiting
            if (isRateLimited(request.getPhoneNumber())) {
                log.warn("Rate limit exceeded for resend: {}", maskPhoneNumber(request.getPhoneNumber()));
                return OtpResponse.builder()
                    .success(false)
                    .message("Too many OTP requests. Please try again later.")
                    .build();
            }
            
            // Generate new OTP
            String newOtp = generateOtp();
            
            // Update existing session or create new one
            Optional<OtpSession> existingSession = otpSessionRepository
                .findActiveSessionByPhoneNumber(request.getPhoneNumber(), LocalDateTime.now());
            
            OtpSession session;
            if (existingSession.isPresent()) {
                session = existingSession.get();
                session.setOtp(newOtp);
                session.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
                session.setAttemptsRemaining(MAX_VERIFICATION_ATTEMPTS);
            } else {
                String sessionId = UUID.randomUUID().toString();
                session = OtpSession.builder()
                    .sessionId(sessionId)
                    .phoneNumber(request.getPhoneNumber())
                    .otp(newOtp)
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .attemptsRemaining(MAX_VERIFICATION_ATTEMPTS)
                    .used(false)
                    .build();
            }
            
            otpSessionRepository.save(session);
            
            // Send new OTP via SMS
            boolean smsSent = smsService.sendOtp(request.getPhoneNumber(), newOtp);
            
            if (!smsSent) {
                log.error("Failed to send SMS OTP to: {}", maskPhoneNumber(request.getPhoneNumber()));
                return OtpResponse.builder()
                    .success(false)
                    .message("Failed to send OTP. Please try again.")
                    .build();
            }
            
            log.info("OTP resent successfully for session: {}", session.getSessionId());
            
            return OtpResponse.builder()
                .success(true)
                .message("OTP resent successfully")
                .sessionId(session.getSessionId())
                .expiryMinutes(OTP_EXPIRY_MINUTES)
                .resendAllowed(true)
                .attemptsRemaining((int) (MAX_OTP_REQUESTS - getOtpRequestCount(request.getPhoneNumber())))
                .build();
                
        } catch (Exception e) {
            log.error("Error resending OTP for phone: {}", maskPhoneNumber(request.getPhoneNumber()), e);
            return OtpResponse.builder()
                .success(false)
                .message("Internal server error. Please try again.")
                .build();
        }
    }

    /**
     * Generates a secure 6-digit OTP.
     * 
     * @return 6-digit OTP string
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Checks if phone number is rate limited.
     * 
     * @param phoneNumber phone number to check
     * @return true if rate limited
     */
    private boolean isRateLimited(String phoneNumber) {
        long requestCount = otpSessionRepository.countByPhoneNumberAndCreatedAtAfter(
            phoneNumber, 
            LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES)
        );
        return requestCount >= MAX_OTP_REQUESTS;
    }

    /**
     * Gets the count of OTP requests for a phone number in the rate limit window.
     * 
     * @param phoneNumber phone number to check
     * @return number of requests
     */
    private long getOtpRequestCount(String phoneNumber) {
        return otpSessionRepository.countByPhoneNumberAndCreatedAtAfter(
            phoneNumber, 
            LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES)
        );
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