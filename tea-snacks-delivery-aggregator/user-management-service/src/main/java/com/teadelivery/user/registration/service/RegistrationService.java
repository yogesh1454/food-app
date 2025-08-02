package com.teadelivery.user.registration.service;

import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.model.UserProfile;
import com.teadelivery.user.profile.repository.UserRepository;
import com.teadelivery.user.auth.service.JwtService;
import com.teadelivery.user.password.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    /**
     * Register user with email and password
     */
    @Transactional
    public UserRegistrationResult registerWithEmail(String email, String password, String name) {
        log.info("Attempting to register user with email: {}", email);

        // Validate inputs
        if (email == null || email.trim().isEmpty()) {
            return UserRegistrationResult.failure("Email is required");
        }
        
        if (password == null || password.trim().isEmpty()) {
            return UserRegistrationResult.failure("Password is required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            return UserRegistrationResult.failure("Name is required");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Email {} already exists", email);
            return UserRegistrationResult.failure("Email already registered");
        }

        // Validate password strength
        PasswordService.PasswordValidationResult passwordValidation = passwordService.validatePasswordStrength(password);
        if (!passwordValidation.isValid()) {
            log.warn("Registration failed: Weak password for email {}", email);
            return UserRegistrationResult.failure(passwordValidation.getMessage());
        }

        // Check for weak passwords
        if (passwordService.isWeakPassword(password)) {
            log.warn("Registration failed: Common weak password for email {}", email);
            return UserRegistrationResult.failure("Password is too common. Please choose a stronger password.");
        }

        try {
            // Encode password
            String encodedPassword = passwordService.encodePassword(password);

            // Create user entity
            User user = User.builder()
                    .email(email)
                    .passwordHash(encodedPassword)
                    .name(name)
                    .userType(User.UserType.REGISTERED)
                    .role(User.Role.CUSTOMER)
                    .status(User.UserStatus.PENDING_VERIFICATION)
                    .emailVerified(false)
                    .phoneVerified(false)
                    .profileCompletionPercentage(25) // Basic info provided
                    .build();

            // Save user
            User savedUser = userRepository.save(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());

            // Create basic user profile
            UserProfile userProfile = UserProfile.builder()
                    .user(savedUser)
                    .firstName(extractFirstName(name))
                    .lastName(extractLastName(name))
                    .build();

            savedUser.setUserProfile(userProfile);

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
            
            String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

            return UserRegistrationResult.success(savedUser, accessToken, refreshToken);

        } catch (Exception e) {
            log.error("Error during user registration for email {}: {}", email, e.getMessage(), e);
            return UserRegistrationResult.failure("Registration failed due to internal error");
        }
    }

    /**
     * Register user with phone number
     */
    @Transactional
    public PhoneRegistrationResult registerWithPhone(String phoneNumber, String name) {
        log.info("Attempting to register user with phone: {}", phoneNumber);

        // Validate inputs
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return PhoneRegistrationResult.failure("Phone number is required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            return PhoneRegistrationResult.failure("Name is required");
        }

        // Check if phone already exists
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            log.warn("Registration failed: Phone {} already exists", phoneNumber);
            return PhoneRegistrationResult.failure("Phone number already registered");
        }

        try {
            // For now, we'll create a pending user and return OTP session
            // In a real implementation, we'd integrate with SMS service
            String sessionId = UUID.randomUUID().toString();
            
            log.info("Phone registration initiated for {}, session: {}", phoneNumber, sessionId);
            
            return PhoneRegistrationResult.success(phoneNumber, sessionId, 5); // 5 minutes expiry

        } catch (Exception e) {
            log.error("Error during phone registration for {}: {}", phoneNumber, e.getMessage(), e);
            return PhoneRegistrationResult.failure("Registration failed due to internal error");
        }
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by phone number
     */
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    /**
     * Authenticate user with email and password
     */
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordService.verifyPassword(password, user.getPasswordHash())) {
                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extract last name from full name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return null;
    }

    /**
     * User registration result
     */
    public static class UserRegistrationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String accessToken;
        private final String refreshToken;

        private UserRegistrationResult(boolean success, String message, User user, String accessToken, String refreshToken) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public static UserRegistrationResult success(User user, String accessToken, String refreshToken) {
            return new UserRegistrationResult(true, "User registered successfully", user, accessToken, refreshToken);
        }

        public static UserRegistrationResult failure(String message) {
            return new UserRegistrationResult(false, message, null, null, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }

    /**
     * Phone registration result
     */
    public static class PhoneRegistrationResult {
        private final boolean success;
        private final String message;
        private final String phoneNumber;
        private final String sessionId;
        private final Integer expiryMinutes;

        private PhoneRegistrationResult(boolean success, String message, String phoneNumber, String sessionId, Integer expiryMinutes) {
            this.success = success;
            this.message = message;
            this.phoneNumber = phoneNumber;
            this.sessionId = sessionId;
            this.expiryMinutes = expiryMinutes;
        }

        public static PhoneRegistrationResult success(String phoneNumber, String sessionId, Integer expiryMinutes) {
            return new PhoneRegistrationResult(true, "OTP sent to phone number", phoneNumber, sessionId, expiryMinutes);
        }

        public static PhoneRegistrationResult failure(String message) {
            return new PhoneRegistrationResult(false, message, null, null, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getSessionId() { return sessionId; }
        public Integer getExpiryMinutes() { return expiryMinutes; }
    }
}
