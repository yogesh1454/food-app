package com.teadelivery.notification.shared.controller;

import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;
import com.teadelivery.notification.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for notification operations.
 * Follows coding standards with comprehensive API endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Sends notification synchronously.
     * 
     * @param request notification request
     * @return notification response
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Received notification request for user: {} with template: {}", 
                request.getUserId(), request.getTemplate());
        
        try {
            NotificationResponse response = notificationService.sendNotification(request);
            
            if (response.getStatus() == NotificationResponse.NotificationStatus.SENT) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing notification request", e);
            
            NotificationResponse errorResponse = NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                request.getUserId(),
                "Internal server error: " + e.getMessage(),
                0
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Sends notification asynchronously.
     * 
     * @param request notification request
     * @return accepted response
     */
    @PostMapping("/send-async")
    public ResponseEntity<String> sendNotificationAsync(@Valid @RequestBody NotificationRequest request) {
        log.info("Received async notification request for user: {} with template: {}", 
                request.getUserId(), request.getTemplate());
        
        try {
            CompletableFuture<NotificationResponse> future = notificationService.sendNotificationAsync(request);
            
            // Don't wait for completion, return immediately
            return ResponseEntity.accepted().body("Notification queued for processing");
            
        } catch (Exception e) {
            log.error("Error queuing async notification request", e);
            return ResponseEntity.internalServerError().body("Failed to queue notification");
        }
    }

    /**
     * Health check endpoint.
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification service is healthy");
    }
}
