package com.teadelivery.user.profile.controller;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.repository.UserRepository;
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
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    @HasPermission(resource = "profile", action = "view", checkOwnership = true, ownerIdParam = "userId")
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
    @HasPermission(resource = "users", action = "view")
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
    @HasPermission(resource = "users", action = "view")
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
    @HasPermission(resource = "users", action = "view")
    public ResponseEntity<List<User>> getActiveUsers() {
        log.info("Getting all active users");
        
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .toList();
        
        return ResponseEntity.ok(activeUsers);
    }

    @PutMapping("/{userId}/status")
    @HasPermission(resource = "users", action = "manage")
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
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User management service is healthy");
    }
} 