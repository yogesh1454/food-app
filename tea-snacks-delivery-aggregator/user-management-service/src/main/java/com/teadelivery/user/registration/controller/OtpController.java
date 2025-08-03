package com.teadelivery.user.registration.controller;

import com.teadelivery.user.registration.dto.OtpRequest;
import com.teadelivery.user.registration.dto.OtpResponse;
import com.teadelivery.user.registration.dto.OtpVerificationRequest;
import com.teadelivery.user.registration.dto.OtpVerificationResponse;
import com.teadelivery.user.registration.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for OTP-related operations.
 * Follows coding standards with proper REST endpoints, validation, and documentation.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/phone")
@RequiredArgsConstructor
@Tag(name = "Phone OTP Registration", description = "APIs for phone number registration with OTP verification")
public class OtpController {

    private final OtpService otpService;

    /**
     * Sends OTP to the specified phone number.
     * 
     * @param request OTP request containing phone number
     * @return OTP response with session details
     */
    @PostMapping("/send-otp")
    @Operation(
        summary = "Send OTP for phone registration",
        description = "Generates and sends a 6-digit OTP to the provided phone number for registration"
    )
    @ApiResponse(
        responseCode = "200",
        description = "OTP sent successfully",
        content = @Content(schema = @Schema(implementation = OtpResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid phone number format or rate limit exceeded",
        content = @Content(schema = @Schema(implementation = OtpResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = OtpResponse.class))
    )
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        log.info("Received OTP request for phone: {}", maskPhoneNumber(request.getPhoneNumber()));
        
        OtpResponse response = otpService.generateAndSendOtp(request);
        
        if (response.isSuccess()) {
            log.info("OTP sent successfully for phone: {}", maskPhoneNumber(request.getPhoneNumber()));
            return ResponseEntity.ok(response);
        } else {
            log.warn("OTP send failed for phone: {} - {}", maskPhoneNumber(request.getPhoneNumber()), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verifies OTP and registers user.
     * 
     * @param request OTP verification request
     * @return OTP verification response with user details
     */
    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP and register user",
        description = "Verifies the OTP and creates a new user account with the provided details"
    )
    @ApiResponse(
        responseCode = "200",
        description = "OTP verified and user registered successfully",
        content = @Content(schema = @Schema(implementation = OtpVerificationResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid OTP, expired session, or validation error",
        content = @Content(schema = @Schema(implementation = OtpVerificationResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = OtpVerificationResponse.class))
    )
    public ResponseEntity<OtpVerificationResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        log.info("Received OTP verification request for session: {}", request.getSessionId());
        
        OtpVerificationResponse response = otpService.verifyOtp(request);
        
        if (response.isSuccess()) {
            log.info("OTP verified successfully for session: {}", request.getSessionId());
            return ResponseEntity.ok(response);
        } else {
            log.warn("OTP verification failed for session: {} - {}", request.getSessionId(), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Resends OTP to the specified phone number.
     * 
     * @param request OTP resend request containing phone number
     * @return OTP response with updated session details
     */
    @PostMapping("/resend-otp")
    @Operation(
        summary = "Resend OTP",
        description = "Generates and sends a new OTP to the provided phone number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "OTP resent successfully",
        content = @Content(schema = @Schema(implementation = OtpResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid phone number format or rate limit exceeded",
        content = @Content(schema = @Schema(implementation = OtpResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = OtpResponse.class))
    )
    public ResponseEntity<OtpResponse> resendOtp(@Valid @RequestBody OtpRequest request) {
        log.info("Received OTP resend request for phone: {}", maskPhoneNumber(request.getPhoneNumber()));
        
        OtpResponse response = otpService.resendOtp(request);
        
        if (response.isSuccess()) {
            log.info("OTP resent successfully for phone: {}", maskPhoneNumber(request.getPhoneNumber()));
            return ResponseEntity.ok(response);
        } else {
            log.warn("OTP resend failed for phone: {} - {}", maskPhoneNumber(request.getPhoneNumber()), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Health check endpoint for OTP service.
     * 
     * @return health status
     */
    @GetMapping("/health")
    @Operation(
        summary = "OTP service health check",
        description = "Check the health status of the OTP service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "OTP service is healthy"
    )
    public ResponseEntity<String> health() {
        log.debug("OTP service health check requested");
        return ResponseEntity.ok("OTP service is healthy");
    }

    /**
     * Masks phone number for logging (privacy protection).
     * 
     * @param phoneNumber phone number to mask
     * @return masked phone number
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "***";
        }
        return phoneNumber.substring(0, 2) + "***" + phoneNumber.substring(phoneNumber.length() - 2);
    }
} 