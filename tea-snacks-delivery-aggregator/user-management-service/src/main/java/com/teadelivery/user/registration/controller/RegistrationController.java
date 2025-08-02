package com.teadelivery.user.registration.controller;

import com.teadelivery.user.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

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
        
        // Use UserService for real registration
        RegistrationService.UserRegistrationResult result = registrationService.registerWithEmail(email, password, name);
        
        if (result.isSuccess()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("userId", result.getUser().getId().toString());
            response.put("email", result.getUser().getEmail());
            response.put("name", result.getUser().getName());
            response.put("accessToken", result.getAccessToken());
            response.put("refreshToken", result.getRefreshToken());
            response.put("userType", result.getUser().getUserType().name());
            response.put("role", result.getUser().getRole().name());
            response.put("status", result.getUser().getStatus().name());
            response.put("profileCompletion", result.getUser().getProfileCompletionPercentage());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(createErrorResponse(result.getMessage()));
        }
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
        
        // Use UserService for real phone registration
        RegistrationService.PhoneRegistrationResult result = registrationService.registerWithPhone(phoneNumber, name);
        
        if (result.isSuccess()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("phoneNumber", result.getPhoneNumber());
            response.put("sessionId", result.getSessionId());
            response.put("expiryMinutes", result.getExpiryMinutes());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(createErrorResponse(result.getMessage()));
        }
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
