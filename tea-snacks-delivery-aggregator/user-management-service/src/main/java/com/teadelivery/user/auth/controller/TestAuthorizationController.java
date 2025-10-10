package com.teadelivery.user.auth.controller;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.profile.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test/auth")
@RequiredArgsConstructor
@Slf4j
public class TestAuthorizationController {
    
    @GetMapping("/admin-only")
    @HasPermission(resource = "users", action = "manage")
    public ResponseEntity<Map<String, Object>> adminOnlyEndpoint() {
        log.info("Admin-only endpoint accessed successfully");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/vendor-menu/{vendorId}")
    @HasPermission(resource = "menu", action = "manage", checkOwnership = true)
    public ResponseEntity<Map<String, Object>> vendorMenuEndpoint(@PathVariable UUID vendorId) {
        log.info("Vendor menu endpoint accessed for vendor: {}", vendorId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Vendor menu access granted");
        response.put("vendorId", vendorId);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer-profile/{userId}")
    @HasPermission(resource = "profile", action = "manage", checkOwnership = true)
    public ResponseEntity<Map<String, Object>> customerProfileEndpoint(@PathVariable UUID userId) {
        log.info("Customer profile endpoint accessed for user: {}", userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer profile access granted");
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/delivery-partner/{partnerId}/deliveries")
    @HasPermission(resource = "deliveries", action = "manage", checkOwnership = true)
    public ResponseEntity<Map<String, Object>> deliveryPartnerEndpoint(@PathVariable UUID partnerId) {
        log.info("Delivery partner endpoint accessed for partner: {}", partnerId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Delivery partner access granted");
        response.put("partnerId", partnerId);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        log.info("Public endpoint accessed");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Public access granted");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
} 