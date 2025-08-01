package com.teadelivery.usermanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registration")
public class SimpleRegistrationController {

    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> registerWithEmail(
            @RequestBody Map<String, String> request) {
        
        String email = request.get("email");
        String password = request.get("password");
        String name = request.get("name");
        
        // Simple validation
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
        }
        
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(createErrorResponse("Password must be at least 6 characters"));
        }
        
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Name is required"));
        }
        
        // Mock successful registration
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("userId", UUID.randomUUID().toString());
        response.put("email", email);
        response.put("name", name);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/phone")
    public ResponseEntity<Map<String, Object>> registerWithPhone(
            @RequestBody Map<String, String> request) {
        
        String phoneNumber = request.get("phoneNumber");
        String name = request.get("name");
        
        // Simple validation
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Phone number is required"));
        }
        
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Name is required"));
        }
        
        // Mock OTP sending
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent to phone number");
        response.put("sessionId", UUID.randomUUID().toString());
        response.put("phoneNumber", phoneNumber);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "user-management-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
