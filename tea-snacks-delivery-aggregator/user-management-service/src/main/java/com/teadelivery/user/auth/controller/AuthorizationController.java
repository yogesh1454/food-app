package com.teadelivery.user.auth.controller;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.auth.service.AuthorizationService;
import com.teadelivery.user.profile.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth/authorization")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {
    
    private final AuthorizationService authorizationService;
    
    @GetMapping("/permissions")
    public ResponseEntity<Set<String>> getCurrentUserPermissions() {
        log.info("Getting permissions for current user");
        Set<String> permissions = authorizationService.getCurrentUserPermissions();
        log.info("Retrieved {} permissions for current user", permissions.size());
        return ResponseEntity.ok(permissions);
    }
    
    @GetMapping("/permissions/{role}")
    @HasPermission(resource = "users", action = "manage")
    public ResponseEntity<Set<String>> getPermissionsForRole(@PathVariable User.Role role) {
        log.info("Getting permissions for role: {}", role);
        Set<String> permissions = authorizationService.getPermissionsForRole(role);
        log.info("Retrieved {} permissions for role {}", permissions.size(), role);
        return ResponseEntity.ok(permissions);
    }
    
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkPermission(
            @RequestParam String resource,
            @RequestParam String action) {
        log.info("Checking permission for current user: {}:{}", resource, action);
        boolean hasPermission = authorizationService.hasPermission(resource, action);
        log.info("Permission check result for {}:{} = {}", resource, action, hasPermission);
        return ResponseEntity.ok(hasPermission);
    }
    
    @PostMapping("/check-resource")
    public ResponseEntity<Boolean> checkResourceAccess(
            @RequestParam String resource,
            @RequestParam String action,
            @RequestParam(required = false) String resourceOwnerId) {
        log.info("Checking resource access for current user: {}:{}", resource, action);
        
        java.util.UUID ownerId = null;
        if (resourceOwnerId != null && !resourceOwnerId.isEmpty()) {
            try {
                ownerId = java.util.UUID.fromString(resourceOwnerId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid resource owner ID format: {}", resourceOwnerId);
                return ResponseEntity.badRequest().body(false);
            }
        }
        
        boolean canAccess = authorizationService.canAccessResource(resource, action, ownerId);
        log.info("Resource access check result for {}:{} = {}", resource, action, canAccess);
        return ResponseEntity.ok(canAccess);
    }
    
    @PostMapping("/initialize")
    @HasPermission(resource = "system", action = "manage")
    public ResponseEntity<String> initializePermissions() {
        log.info("Initializing default permissions");
        authorizationService.initializeDefaultPermissions();
        log.info("Default permissions initialized successfully");
        return ResponseEntity.ok("Default permissions initialized successfully");
    }
} 