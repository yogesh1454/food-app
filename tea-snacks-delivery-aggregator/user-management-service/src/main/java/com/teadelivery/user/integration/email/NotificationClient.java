package com.teadelivery.user.integration.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client for communicating with the Notification Service.
 * Handles email and SMS notifications for user management workflows.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.service.url:http://localhost:8084}")
    private String notificationServiceUrl;
    
    @Value("${app.base-url:http://localhost:8082}")
    private String appBaseUrl;

    /**
     * Send registration verification email to user
     */
    public void sendRegistrationVerificationEmail(UUID userId, String email, String userName, String verificationCode, String verificationLink) {
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("userId", userId.toString());
            notificationRequest.put("type", "EMAIL");
            notificationRequest.put("template", "registration-verification");
            notificationRequest.put("recipient", email);
            notificationRequest.put("subject", "Welcome to Tea & Snacks - Verify Your Email");
            notificationRequest.put("priority", "HIGH");
            notificationRequest.put("trackDelivery", true);

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("verificationCode", verificationCode);
            // Use localhost URL for development
            String localVerificationLink = appBaseUrl + "/verify?token=" + verificationCode + "&email=" + email;
            variables.put("verificationLink", localVerificationLink);
            notificationRequest.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationRequest, headers);

            String url = notificationServiceUrl + "/api/notifications/send-async";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Registration verification email queued successfully for user: {} to email: {}", userId, email);
            } else {
                log.error("Failed to send registration verification email for user: {} to email: {}. Status: {}", 
                         userId, email, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending registration verification email for user: {} to email: {}", userId, email, e);
        }
    }

    /**
     * Send password reset email to user
     */
    public void sendPasswordResetEmail(UUID userId, String email, String userName, String resetLink) {
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("userId", userId.toString());
            notificationRequest.put("type", "EMAIL");
            notificationRequest.put("template", "password-reset");
            notificationRequest.put("recipient", email);
            notificationRequest.put("subject", "Tea & Snacks - Password Reset Request");
            notificationRequest.put("priority", "URGENT");
            notificationRequest.put("trackDelivery", true);

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            // Use localhost URL for development  
            String localResetLink = appBaseUrl + "/reset-password?token=" + resetLink.substring(resetLink.lastIndexOf("=") + 1) + "&email=" + email;
            variables.put("resetLink", localResetLink);
            variables.put("expiryTime", "24 hours");
            notificationRequest.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationRequest, headers);

            String url = notificationServiceUrl + "/api/notifications/send-async";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Password reset email queued successfully for user: {} to email: {}", userId, email);
            } else {
                log.error("Failed to send password reset email for user: {} to email: {}. Status: {}", 
                         userId, email, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending password reset email for user: {} to email: {}", userId, email, e);
        }
    }

    /**
     * Send welcome email after successful registration
     */
    public void sendWelcomeEmail(UUID userId, String email, String userName) {
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("userId", userId.toString());
            notificationRequest.put("type", "EMAIL");
            notificationRequest.put("template", "registration-verification"); // Reuse template for now
            notificationRequest.put("recipient", email);
            notificationRequest.put("subject", "Welcome to Tea & Snacks Platform!");
            notificationRequest.put("priority", "NORMAL");
            notificationRequest.put("trackDelivery", true);

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("welcomeMessage", "Thank you for joining Tea & Snacks! Your account is now active.");
            notificationRequest.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationRequest, headers);

            String url = notificationServiceUrl + "/api/notifications/send-async";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Welcome email queued successfully for user: {} to email: {}", userId, email);
            } else {
                log.error("Failed to send welcome email for user: {} to email: {}. Status: {}", 
                         userId, email, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending welcome email for user: {} to email: {}", userId, email, e);
        }
    }
}
