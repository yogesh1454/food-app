package com.teadelivery.user.guest.service;

import com.teadelivery.user.guest.dto.GuestUserRequest;
import com.teadelivery.user.guest.dto.GuestUserResponse;
import com.teadelivery.user.guest.dto.GuestSessionResponse;
import com.teadelivery.user.guest.model.GuestUser;
import com.teadelivery.user.guest.repository.GuestUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing guest user operations.
 * Follows coding standards with proper logging, error handling, and immutability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuestUserService {

    private final GuestUserRepository guestUserRepository;
    private final DeviceFingerprintService deviceFingerprintService;
    
    private static final int GUEST_SESSION_HOURS = 24;
    private static final int MAX_GUEST_SESSIONS_PER_DAY = 1;
    private static final int CONVERSION_PROMPT_THRESHOLD = 5;
    private static final List<String> GUEST_LIMITATIONS = Arrays.asList(
        "cannot_place_orders",
        "cannot_save_favorites", 
        "limited_search_history",
        "cannot_write_reviews",
        "cannot_access_order_history"
    );

    /**
     * Creates a new guest user account.
     * 
     * @param request guest user creation request
     * @return guest user response
     */
    @Transactional
    public GuestUserResponse createGuestUser(GuestUserRequest request) {
        log.info("Creating guest user for device: {}", maskDeviceId(request.getDeviceId()));
        
        try {
            // Validate device fingerprint
            if (!deviceFingerprintService.isValidDeviceId(request.getDeviceId())) {
                log.warn("Invalid device ID: {}", request.getDeviceId());
                return GuestUserResponse.builder()
                    .success(false)
                    .message("Invalid device identifier")
                    .build();
            }

            // Check if device already has active guest session
            if (guestUserRepository.existsActiveByDeviceId(request.getDeviceId(), LocalDateTime.now())) {
                log.warn("Device already has active guest session: {}", maskDeviceId(request.getDeviceId()));
                return GuestUserResponse.builder()
                    .success(false)
                    .message("Device already has an active guest session")
                    .build();
            }

            // Check daily limit
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            List<GuestUser> recentSessions = guestUserRepository.findByDeviceIdAndCreatedAtAfter(
                request.getDeviceId(), since);
            
            if (recentSessions.size() >= MAX_GUEST_SESSIONS_PER_DAY) {
                log.warn("Daily guest session limit exceeded for device: {}", maskDeviceId(request.getDeviceId()));
                return GuestUserResponse.builder()
                    .success(false)
                    .message("Daily guest session limit exceeded. Please register for full access.")
                    .build();
            }

            // Create guest user
            String sessionToken = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(GUEST_SESSION_HOURS);
            
            GuestUser guestUser = GuestUser.builder()
                .deviceId(request.getDeviceId())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .platform(request.getSessionMetadata() != null ? request.getSessionMetadata().getPlatform() : null)
                .version(request.getSessionMetadata() != null ? request.getSessionMetadata().getVersion() : null)
                .sessionToken(sessionToken)
                .expiresAt(expiresAt)
                .lastActivityAt(LocalDateTime.now())
                .actionCount(0)
                .conversionPromptsShown(0)
                .isActive(true)
                .build();
            
            guestUserRepository.save(guestUser);
            
            log.info("Guest user created successfully: {}", guestUser.getId());
            
            return GuestUserResponse.builder()
                .success(true)
                .message("Guest account created")
                .data(GuestUserResponse.GuestUserData.builder()
                    .guestUserId(guestUser.getId().toString())
                    .sessionToken(sessionToken)
                    .userType("GUEST")
                    .expiryTime(expiresAt)
                    .limitations(GUEST_LIMITATIONS)
                    .actionCount(0)
                    .conversionPromptsShown(0)
                    .build())
                .build();
                
        } catch (Exception e) {
            log.error("Error creating guest user for device: {}", maskDeviceId(request.getDeviceId()), e);
            return GuestUserResponse.builder()
                .success(false)
                .message("Internal server error. Please try again.")
                .build();
        }
    }

    /**
     * Gets guest session information.
     * 
     * @param sessionToken the session token
     * @return guest session response
     */
    @Transactional(readOnly = true)
    public GuestSessionResponse getGuestSession(String sessionToken) {
        log.info("Getting guest session info for token: {}", maskSessionToken(sessionToken));
        
        try {
            Optional<GuestUser> guestUserOpt = guestUserRepository.findActiveBySessionToken(
                sessionToken, LocalDateTime.now());
            
            if (guestUserOpt.isEmpty()) {
                log.warn("Invalid or expired session token: {}", maskSessionToken(sessionToken));
                return GuestSessionResponse.builder()
                    .success(false)
                    .message("Invalid or expired session")
                    .build();
            }
            
            GuestUser guestUser = guestUserOpt.get();
            
            // Update last activity
            guestUser.updateLastActivity();
            guestUserRepository.save(guestUser);
            
            // Calculate time remaining
            Duration timeRemaining = Duration.between(LocalDateTime.now(), guestUser.getExpiresAt());
            String timeRemainingStr = formatDuration(timeRemaining);
            
            // Determine session status
            String sessionStatus = guestUser.isActive() ? "active" : "expired";
            
            log.info("Guest session info retrieved for user: {}", guestUser.getId());
            
            return GuestSessionResponse.builder()
                .success(true)
                .message("Guest session info retrieved")
                .data(GuestSessionResponse.GuestSessionData.builder()
                    .guestUserId(guestUser.getId().toString())
                    .sessionStatus(sessionStatus)
                    .expiryTime(guestUser.getExpiresAt())
                    .timeRemaining(timeRemainingStr)
                    .limitations(GUEST_LIMITATIONS)
                    .conversionPrompts(GuestSessionResponse.ConversionPrompts.builder()
                        .showAfterActions(CONVERSION_PROMPT_THRESHOLD)
                        .currentActionCount(guestUser.getActionCount())
                        .build())
                    .build())
                .build();
                
        } catch (Exception e) {
            log.error("Error getting guest session for token: {}", maskSessionToken(sessionToken), e);
            return GuestSessionResponse.builder()
                .success(false)
                .message("Internal server error. Please try again.")
                .build();
        }
    }

    /**
     * Records a guest user action and checks if conversion prompt should be shown.
     * 
     * @param sessionToken the session token
     * @return true if conversion prompt should be shown
     */
    @Transactional
    public boolean recordGuestAction(String sessionToken) {
        log.debug("Recording guest action for session: {}", maskSessionToken(sessionToken));
        
        try {
            Optional<GuestUser> guestUserOpt = guestUserRepository.findActiveBySessionToken(
                sessionToken, LocalDateTime.now());
            
            if (guestUserOpt.isEmpty()) {
                log.warn("Cannot record action for invalid session: {}", maskSessionToken(sessionToken));
                return false;
            }
            
            GuestUser guestUser = guestUserOpt.get();
            guestUser.incrementActionCount();
            guestUser.updateLastActivity();
            guestUserRepository.save(guestUser);
            
            // Check if conversion prompt should be shown
            boolean shouldShowPrompt = guestUser.getActionCount() >= CONVERSION_PROMPT_THRESHOLD;
            
            if (shouldShowPrompt) {
                log.info("Conversion prompt should be shown for guest user: {}", guestUser.getId());
            }
            
            return shouldShowPrompt;
            
        } catch (Exception e) {
            log.error("Error recording guest action for session: {}", maskSessionToken(sessionToken), e);
            return false;
        }
    }

    /**
     * Records that a conversion prompt was shown to the guest user.
     * 
     * @param sessionToken the session token
     * @return true if recorded successfully
     */
    @Transactional
    public boolean recordConversionPromptShown(String sessionToken) {
        log.debug("Recording conversion prompt shown for session: {}", maskSessionToken(sessionToken));
        
        try {
            Optional<GuestUser> guestUserOpt = guestUserRepository.findActiveBySessionToken(
                sessionToken, LocalDateTime.now());
            
            if (guestUserOpt.isEmpty()) {
                log.warn("Cannot record conversion prompt for invalid session: {}", maskSessionToken(sessionToken));
                return false;
            }
            
            GuestUser guestUser = guestUserOpt.get();
            guestUser.incrementConversionPromptsShown();
            guestUserRepository.save(guestUser);
            
            log.debug("Conversion prompt recorded for guest user: {}", guestUser.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error recording conversion prompt for session: {}", maskSessionToken(sessionToken), e);
            return false;
        }
    }

    /**
     * Validates guest session token.
     * 
     * @param sessionToken the session token
     * @return true if valid
     */
    @Transactional(readOnly = true)
    public boolean isValidGuestSession(String sessionToken) {
        return guestUserRepository.findActiveBySessionToken(sessionToken, LocalDateTime.now()).isPresent();
    }

    /**
     * Gets guest user by session token.
     * 
     * @param sessionToken the session token
     * @return optional guest user
     */
    @Transactional(readOnly = true)
    public Optional<GuestUser> getGuestUserBySessionToken(String sessionToken) {
        return guestUserRepository.findActiveBySessionToken(sessionToken, LocalDateTime.now());
    }

    /**
     * Formats duration for display.
     * 
     * @param duration the duration to format
     * @return formatted duration string
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
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

    /**
     * Masks session token for logging (privacy protection).
     * 
     * @param sessionToken session token to mask
     * @return masked session token
     */
    private String maskSessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.length() < 8) {
            return "***";
        }
        return sessionToken.substring(0, 4) + "***" + sessionToken.substring(sessionToken.length() - 4);
    }
} 