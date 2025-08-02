package com.teadelivery.user.registration.controller;

import com.teadelivery.user.registration.dto.ApiResponse;
import com.teadelivery.user.registration.dto.EmailRegistrationRequest;
import com.teadelivery.user.registration.dto.PhoneRegistrationRequest;
import com.teadelivery.user.registration.dto.PhoneRegistrationResponse;
import com.teadelivery.user.registration.dto.RegistrationResponse;
import com.teadelivery.user.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Registration", description = "APIs for user registration with email and phone")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/email")
    @Operation(summary = "Register user with email", description = "Register a new user using email and password")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully", 
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or registration failed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerWithEmail(
            @Valid @RequestBody EmailRegistrationRequest request) {
        
        log.info("Attempting email registration for user: {}", request.getEmail());
        
        try {
            // Use RegistrationService for email registration
            RegistrationService.UserRegistrationResult result = registrationService.registerWithEmail(
                request.getEmail(), 
                request.getPassword(), 
                request.getName()
            );
            
            if (result.isSuccess()) {
                log.info("Email registration successful for user: {}", request.getEmail());
                
                RegistrationResponse registrationResponse = RegistrationResponse.builder()
                    .userId(result.getUser().getId().toString())
                    .email(result.getUser().getEmail())
                    .name(result.getUser().getName())
                    .userType(result.getUser().getUserType().name())
                    .role(result.getUser().getRole().name())
                    .status(result.getUser().getStatus().name())
                    .profileCompletion(result.getUser().getProfileCompletionPercentage())
                    .accessToken(result.getAccessToken())
                    .refreshToken(result.getRefreshToken())
                    .build();
                
                ApiResponse<RegistrationResponse> response = ApiResponse.<RegistrationResponse>builder()
                    .success(true)
                    .message(result.getMessage())
                    .data(registrationResponse)
                    .timestamp(Instant.now())
                    .build();
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("Email registration failed for user: {} - {}", request.getEmail(), result.getMessage());
                
                ApiResponse<RegistrationResponse> response = ApiResponse.<RegistrationResponse>builder()
                    .success(false)
                    .message(result.getMessage())
                    .timestamp(Instant.now())
                    .build();
                
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error during email registration for user: {}", request.getEmail(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @PostMapping("/phone")
    @Operation(summary = "Register user with phone", description = "Register a new user using phone number and send OTP")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully", 
                    content = @Content(schema = @Schema(implementation = PhoneRegistrationResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or registration failed",
                    content = @Content(schema = @Schema(implementation = PhoneRegistrationResponse.class)))
    })
    public ResponseEntity<PhoneRegistrationResponse> registerWithPhone(
            @Valid @RequestBody PhoneRegistrationRequest request) {
        
        String phoneNumber = request.getPhoneNumber();
        String name = request.getName();
        
        // Use UserService for real phone registration
        RegistrationService.PhoneRegistrationResult result = registrationService.registerWithPhone(phoneNumber, name);
        
        if (result.isSuccess()) {
            PhoneRegistrationResponse response = PhoneRegistrationResponse.builder()
                .success(true)
                .message(result.getMessage())
                .phoneNumber(result.getPhoneNumber())
                .sessionId(result.getSessionId())
                .expiryMinutes(result.getExpiryMinutes())
                .build();
            
            return ResponseEntity.ok(response);
        } else {
            PhoneRegistrationResponse response = PhoneRegistrationResponse.builder()
                .success(false)
                .message(result.getMessage())
                .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Service health check", description = "Check the health status of the user management service")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service is healthy", 
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
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
