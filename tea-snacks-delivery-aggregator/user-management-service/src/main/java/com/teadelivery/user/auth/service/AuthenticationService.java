package com.teadelivery.user.auth.service;

import com.teadelivery.user.config.JwtConfig;
import com.teadelivery.user.auth.dto.LoginRequest;
import com.teadelivery.user.auth.dto.LoginResponse;
import com.teadelivery.user.auth.dto.RefreshTokenRequest;
import com.teadelivery.user.auth.dto.RefreshTokenResponse;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Authentication service for user login, logout, and token management.
 * Follows coding standards with comprehensive authentication logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    /**
     * Authenticates user and generates tokens.
     * 
     * @param request login request
     * @return login response with tokens
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", maskUsername(request.getUsername()));

        try {
            // Find user by email or phone
            User user = userRepository.findByEmailOrPhoneNumber(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Check if user is active
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("Login attempt for inactive account: {}", maskUsername(request.getUsername()));
                throw new BadCredentialsException("Account is not active. Please contact support.");
            }

            // Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                handleFailedLogin(user);
                throw new BadCredentialsException("Invalid credentials");
            }

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(
                    user.getId().toString(),
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                    user.getRole().name()
            );
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

            log.info("Successful login for user: {}", user.getId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtConfig.getAccessTokenExpiration())
                    .userId(user.getId().toString())
                    .username(user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", maskUsername(request.getUsername()));
            throw e;
        } catch (Exception e) {
            log.error("Error during login for username: {}", maskUsername(request.getUsername()), e);
            throw new RuntimeException("Authentication failed", e);
        }
    }

    /**
     * Refreshes access token using refresh token.
     * 
     * @param request refresh token request
     * @return refresh token response
     */
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        try {
            // Validate refresh token
            if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            // Check if token is expired
            if (jwtTokenProvider.isTokenExpired(request.getRefreshToken())) {
                throw new BadCredentialsException("Refresh token expired");
            }

            // Extract user ID from refresh token
            String userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());

            // Find user
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new BadCredentialsException("User account is inactive");
            }

            // Generate new access token
            String newAccessToken = jwtTokenProvider.generateAccessToken(
                    user.getId().toString(),
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                    user.getRole().name()
            );

            log.info("Token refreshed successfully for user: {}", user.getId());

            return RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .expiresIn(jwtConfig.getAccessTokenExpiration())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed token refresh: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during token refresh", e);
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * Logs out user by invalidating tokens.
     * 
     * @param accessToken access token to invalidate
     * @return logout response
     */
    public String logout(String accessToken) {
        log.info("Logout attempt");

        try {
            // Validate access token
            if (!jwtTokenProvider.validateToken(accessToken)) {
                throw new BadCredentialsException("Invalid access token");
            }

            // Extract user ID from token
            String userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            // TODO: Add token to blacklist (Redis implementation)
            // For now, we'll just log the logout
            log.info("User logged out successfully: {}", userId);

            return "Successfully logged out";

        } catch (BadCredentialsException e) {
            log.warn("Failed logout: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new RuntimeException("Logout failed", e);
        }
    }

    /**
     * Creates user account from OTP verification.
     * 
     * @param username username (email or phone)
     * @param password encoded password
     * @param firstName first name
     * @param lastName last name
     * @param email email (if available)
     * @param phoneNumber phone number (if available)
     * @return created user
     */
    @Transactional
    public User createUserFromOtpVerification(String username, String password, 
                                            String firstName, String lastName, 
                                            String email, String phoneNumber) {
        log.info("Creating user account from OTP verification: {}", maskUsername(username));

        // Check if user already exists
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("User already exists with phone: " + phoneNumber);
        }

        // Create user
        User user = User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .passwordHash(password)
                .name(firstName + " " + lastName)
                .userType(User.UserType.REGISTERED)
                .role(User.Role.CUSTOMER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(email != null)
                .phoneVerified(phoneNumber != null)
                .profileCompletionPercentage(25)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User account created successfully: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Converts guest user to registered user.
     * 
     * @param guestUserId guest user ID
     * @param username username (email or phone)
     * @param password encoded password
     * @param firstName first name
     * @param lastName last name
     * @param email email (if available)
     * @param phoneNumber phone number (if available)
     * @return converted user
     */
    @Transactional
    public User convertGuestToRegisteredUser(UUID guestUserId, String username, String password,
                                           String firstName, String lastName,
                                           String email, String phoneNumber) {
        log.info("Converting guest user to registered user: {}", guestUserId);

        // Check if user already exists
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("User already exists with phone: " + phoneNumber);
        }

        // Create user with guest user reference
        User user = User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .passwordHash(password)
                .name(firstName + " " + lastName)
                .userType(User.UserType.REGISTERED)
                .role(User.Role.CUSTOMER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(email != null)
                .phoneVerified(phoneNumber != null)
                .profileCompletionPercentage(25)
                .convertedFromGuestId(guestUserId)
                .conversionDate(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Guest user converted to registered user: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Handles failed login attempt.
     * 
     * @param user user who failed login
     */
    private void handleFailedLogin(User user) {
        // For now, just log the failed attempt
        // TODO: Implement proper failed login tracking in the User model
        log.warn("Failed login attempt for user: {}", user.getId());
    }

    /**
     * Masks username for logging (privacy protection).
     * 
     * @param username username to mask
     * @return masked username
     */
    private String maskUsername(String username) {
        if (username == null || username.length() < 3) {
            return "***";
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 1);
    }

    /**
     * Creates a new user from email registration.
     * 
     * @param email user email
     * @param password user password
     * @param name user name
     * @param phoneNumber user phone number
     * @return created user
     */
    @Transactional
    public User createUserFromEmailRegistration(String email, String password, String name, String phoneNumber) {
        log.info("Creating user account from email registration: {}", maskEmail(email));

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("User already exists with phone: " + phoneNumber);
        }

        // Create user
        User user = User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .passwordHash(passwordEncoder.encode(password))
                .name(name)
                .userType(User.UserType.REGISTERED)
                .role(User.Role.CUSTOMER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(phoneNumber != null)
                .profileCompletionPercentage(25) // Initial completion
                .build();

        User savedUser = userRepository.save(user);
        log.info("User account created successfully: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Generates access token for a user.
     * 
     * @param user user to generate token for
     * @return access token
     */
    public String generateTokensForUser(User user) {
        return jwtTokenProvider.generateAccessToken(
                user.getId().toString(),
                user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                user.getRole().name()
        );
    }

    /**
     * Generates refresh token for a user.
     * 
     * @param user user to generate token for
     * @return refresh token
     */
    public String generateRefreshTokenForUser(User user) {
        return jwtTokenProvider.generateRefreshToken(user.getId().toString());
    }

    /**
     * Masks email for logging (privacy protection).
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