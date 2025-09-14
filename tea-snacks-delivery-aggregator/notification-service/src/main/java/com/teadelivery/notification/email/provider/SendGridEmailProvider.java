package com.teadelivery.notification.email.provider;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.teadelivery.notification.config.NotificationConfig;
import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * SendGrid email provider implementation.
 * Follows coding standards with comprehensive email sending.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "sendgrid", matchIfMissing = true)
public class SendGridEmailProvider implements EmailProvider {

    private final NotificationConfig notificationConfig;

    @Override
    public NotificationResponse sendEmail(NotificationRequest request, String content) {
        UUID notificationId = UUID.randomUUID();
        
        try {
            // Validate configuration
            if (!isConfigurationValid()) {
                return NotificationResponse.failure(
                    notificationId,
                    request.getUserId(),
                    "SendGrid configuration is invalid",
                    0
                );
            }

            // Create SendGrid mail object
            Mail mail = createMail(request, content);
            
            // Send email
            SendGrid sg = new SendGrid(notificationConfig.getEmail().getApiKey());
            Request sgRequest = new Request();
            
            sgRequest.setMethod(Method.POST);
            sgRequest.setEndpoint("mail/send");
            sgRequest.setBody(mail.build());
            
            Response response = sg.api(sgRequest);
            
            // Process response
            return processResponse(notificationId, request.getUserId(), response);
            
        } catch (IOException e) {
            log.error("Failed to send email via SendGrid for user: {}", request.getUserId(), e);
            return NotificationResponse.failure(
                notificationId,
                request.getUserId(),
                "SendGrid API error: " + e.getMessage(),
                0
            );
        } catch (Exception e) {
            log.error("Unexpected error sending email via SendGrid for user: {}", request.getUserId(), e);
            return NotificationResponse.failure(
                notificationId,
                request.getUserId(),
                "Unexpected error: " + e.getMessage(),
                0
            );
        }
    }

    @Override
    public boolean isConfigurationValid() {
        NotificationConfig.Email emailConfig = notificationConfig.getEmail();
        
        boolean isValid = StringUtils.hasText(emailConfig.getApiKey()) &&
                         StringUtils.hasText(emailConfig.getFromEmail()) &&
                         StringUtils.hasText(emailConfig.getFromName());
        
        if (!isValid) {
            log.warn("SendGrid configuration is invalid - missing required properties");
        }
        
        return isValid;
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }

    /**
     * Creates SendGrid Mail object from notification request.
     * 
     * @param request notification request
     * @param content email content
     * @return Mail object
     */
    private Mail createMail(NotificationRequest request, String content) {
        NotificationConfig.Email emailConfig = notificationConfig.getEmail();
        
        Email from = new Email(emailConfig.getFromEmail(), emailConfig.getFromName());
        Email to = new Email(request.getRecipient());
        
        String subject = request.getSubject() != null ? 
            request.getSubject() : 
            "Notification from " + emailConfig.getFromName();
        
        Content emailContent = new Content("text/html", content);
        
        Mail mail = new Mail(from, subject, to, emailContent);
        
        // Add custom headers for tracking
        mail.addCustomArg("template", request.getTemplate());
        if (request.getUserId() != null) {
            mail.addCustomArg("user_id", request.getUserId().toString());
        }
        
        log.debug("Created SendGrid mail for recipient: {} with template: {}", 
                 request.getRecipient(), request.getTemplate());
        
        return mail;
    }

    /**
     * Processes SendGrid API response.
     * 
     * @param notificationId notification ID
     * @param userId user ID
     * @param response SendGrid response
     * @return notification response
     */
    private NotificationResponse processResponse(UUID notificationId, UUID userId, Response response) {
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody();
        
        log.debug("SendGrid response - Status: {}, Body: {}", statusCode, responseBody);
        
        if (statusCode >= 200 && statusCode < 300) {
            // Success
            String messageId = extractMessageId(response);
            
            log.info("Email sent successfully via SendGrid - Status: {}, MessageId: {}", 
                    statusCode, messageId);
            
            return NotificationResponse.builder()
                    .notificationId(notificationId)
                    .userId(userId)
                    .status(NotificationResponse.NotificationStatus.SENT)
                    .message("Email sent successfully")
                    .providerResponse(responseBody)
                    .providerMessageId(messageId)
                    .sentAt(java.time.Instant.now())
                    .retryCount(0)
                    .build();
        } else {
            // Failure
            String errorMessage = String.format("SendGrid API error - Status: %d, Body: %s", 
                                               statusCode, responseBody);
            
            log.warn("Failed to send email via SendGrid - {}", errorMessage);
            
            return NotificationResponse.failure(notificationId, userId, errorMessage, 0);
        }
    }

    /**
     * Extracts message ID from SendGrid response headers.
     * 
     * @param response SendGrid response
     * @return message ID or null
     */
    private String extractMessageId(Response response) {
        try {
            // SendGrid returns message ID in X-Message-Id header
            return response.getHeaders().get("X-Message-Id");
        } catch (Exception e) {
            log.debug("Could not extract message ID from SendGrid response", e);
            return null;
        }
    }
}
