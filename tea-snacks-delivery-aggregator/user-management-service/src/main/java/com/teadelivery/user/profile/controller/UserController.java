package com.teadelivery.user.profile.controller;

import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and management endpoints")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves user profile by user ID")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        log.info("Getting user by ID: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves user profile by email address")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        log.info("Getting user by email: {}", email);
        
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Get user by phone number", description = "Retrieves user profile by phone number")
    public ResponseEntity<User> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        log.info("Getting user by phone number: {}", phoneNumber);
        
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active users", description = "Retrieves all active users in the system")
    public ResponseEntity<List<User>> getActiveUsers() {
        log.info("Getting all active users");
        
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .toList();
        
        return ResponseEntity.ok(activeUsers);
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Updates the status of a user")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam User.UserStatus status) {
        log.info("Updating user status: {} to {}", userId, status);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status);
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "User service health check", description = "Health check for user management service")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User management service is healthy");
    }
} 