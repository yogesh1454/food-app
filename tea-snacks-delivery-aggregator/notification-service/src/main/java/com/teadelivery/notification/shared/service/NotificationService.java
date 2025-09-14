package com.teadelivery.notification.shared.service;

import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;
import com.teadelivery.notification.tracking.model.NotificationLog;
import com.teadelivery.notification.email.provider.EmailProvider;
import com.teadelivery.notification.sms.provider.SmsProvider;
import com.teadelivery.notification.tracking.repository.NotificationLogRepository;
import com.teadelivery.notification.template.service.TemplateService;
import com.teadelivery.notification.tracking.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Core notification service for sending notifications.
 * Follows coding standards with comprehensive notification processing.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailProvider emailProvider;
    private final SmsProvider smsProvider;
    private final TemplateService templateService;
    private final RateLimitService rateLimitService;
    private final NotificationLogRepository notificationLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Sends notification synchronously.
     * 
     * @param request notification request
     * @return notification response
     */
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        logger.info("Sending {} notification to {} using template: {}", 
                request.getType(), request.getRecipient(), request.getTemplate());
        
        // Validate request
        NotificationResponse validationResult = validateRequest(request);
        if (validationResult != null) {
            return validationResult;
        }
        
        // Check rate limits
        if (!rateLimitService.isAllowed(request)) {
            NotificationResponse rateLimitResponse = NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                request.getUserId(),
                "Rate limit exceeded",
                0
            );
            logNotification(request, rateLimitResponse);
            return rateLimitResponse;
        }
        
        // Create log entry
        NotificationLog log = NotificationLog.fromRequest(request);
        log = notificationLogRepository.save(log);
        
        try {
            // Render template
            String content = templateService.renderTemplate(request);
            
            // Send notification based on type
            NotificationResponse response = sendByType(request, content);
            
            // Update log with response
            log.updateFromResponse(response);
            notificationLogRepository.save(log);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to send notification for user: {}", request.getUserId(), e);
            
            NotificationResponse errorResponse = NotificationResponse.failure(
                log.getId(),
                request.getUserId(),
                "Failed to send notification: " + e.getMessage(),
                0
            );
            
            log.updateFromResponse(errorResponse);
            notificationLogRepository.save(log);
            
            return errorResponse;
        }
    }

    /**
     * Sends notification asynchronously.
     * 
     * @param request notification request
     * @return future notification response
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<NotificationResponse> sendNotificationAsync(NotificationRequest request) {
        logger.info("Sending async {} notification to {} using template: {}", 
                request.getType(), request.getRecipient(), request.getTemplate());
        
        try {
            NotificationResponse response = sendNotification(request);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            logger.error("Failed to send async notification for user: {}", request.getUserId(), e);
            
            NotificationResponse errorResponse = NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                request.getUserId(),
                "Failed to send async notification: " + e.getMessage(),
                0
            );
            
            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    /**
     * Validates notification request.
     * 
     * @param request notification request
     * @return validation error response or null if valid
     */
    private NotificationResponse validateRequest(NotificationRequest request) {
        if (request == null) {
            return NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                null,
                "Notification request is null",
                0
            );
        }
        
        if (request.getType() == null) {
            return NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                request.getUserId(),
                "Notification type is required",
                0
            );
        }
        
        if (request.getRecipient() == null || request.getRecipient().trim().isEmpty()) {
            return NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                request.getUserId(),
                "Recipient is required",
                0
            );
        }
        
        if (request.getTemplate() == null || request.getTemplate().trim().isEmpty()) {
            return NotificationResponse.failure(
                java.util.UUID.randomUUID(),
                request.getUserId(),
                "Template is required",
                0
            );
        }
        
        return null; // Valid
    }

    /**
     * Sends notification based on type.
     * 
     * @param request notification request
     * @param content rendered content
     * @return notification response
     */
    private NotificationResponse sendByType(NotificationRequest request, String content) {
        switch (request.getType()) {
            case EMAIL:
                if (!emailProvider.isConfigurationValid()) {
                    throw new IllegalStateException("Email provider configuration is invalid");
                }
                return emailProvider.sendEmail(request, content);
                
            case SMS:
                if (!smsProvider.isConfigurationValid()) {
                    throw new IllegalStateException("SMS provider configuration is invalid");
                }
                return smsProvider.sendSms(request, content);
                
            default:
                throw new UnsupportedOperationException("Unsupported notification type: " + request.getType());
        }
    }

    /**
     * Logs notification attempt.
     * 
     * @param request notification request
     * @param response notification response
     */
    private void logNotification(NotificationRequest request, NotificationResponse response) {
        try {
            NotificationLog log = NotificationLog.fromRequest(request);
            log.updateFromResponse(response);
            notificationLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to log notification for user: {}", request.getUserId(), e);
        }
    }
}
