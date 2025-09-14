package com.teadelivery.notification.sms.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.notification.config.NotificationConfig;
import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.UUID;

/**
 * Gupshup SMS provider implementation.
 * Follows coding standards with comprehensive SMS sending.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "gupshup", matchIfMissing = true)
public class GupshupSmsProvider implements SmsProvider {

    private final NotificationConfig notificationConfig;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public GupshupSmsProvider(NotificationConfig notificationConfig, ObjectMapper objectMapper) {
        this.notificationConfig = notificationConfig;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(notificationConfig.getSms().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Override
    public NotificationResponse sendSms(NotificationRequest request, String content) {
        UUID notificationId = UUID.randomUUID();
        
        try {
            // Validate configuration
            if (!isConfigurationValid()) {
                return NotificationResponse.failure(
                    notificationId,
                    request.getUserId(),
                    "Gupshup configuration is invalid",
                    0
                );
            }

            // Create request body
            MultiValueMap<String, String> formData = createFormData(request, content);
            
            // Send SMS
            String response = webClient.post()
                    .uri("/send")
                    .header("apikey", notificationConfig.getSms().getApiKey())
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            // Process response
            return processResponse(notificationId, request.getUserId(), response);
            
        } catch (WebClientResponseException e) {
            log.error("Gupshup API error for user: {} - Status: {}, Body: {}", 
                     request.getUserId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            return NotificationResponse.failure(
                notificationId,
                request.getUserId(),
                "Gupshup API error: " + e.getMessage(),
                0
            );
        } catch (Exception e) {
            log.error("Unexpected error sending SMS via Gupshup for user: {}", request.getUserId(), e);
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
        NotificationConfig.Sms smsConfig = notificationConfig.getSms();
        
        boolean isValid = StringUtils.hasText(smsConfig.getApiKey()) &&
                         StringUtils.hasText(smsConfig.getSenderId()) &&
                         StringUtils.hasText(smsConfig.getBaseUrl());
        
        if (!isValid) {
            log.warn("Gupshup configuration is invalid - missing required properties");
        }
        
        return isValid;
    }

    @Override
    public String getProviderName() {
        return "Gupshup";
    }

    /**
     * Creates form data for Gupshup API request.
     * 
     * @param request notification request
     * @param content SMS content
     * @return form data
     */
    private MultiValueMap<String, String> createFormData(NotificationRequest request, String content) {
        NotificationConfig.Sms smsConfig = notificationConfig.getSms();
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("channel", "sms");
        formData.add("source", smsConfig.getSenderId());
        formData.add("destination", request.getRecipient());
        formData.add("message", content);
        formData.add("src.name", "TeaSnacks");
        
        // Add custom parameters for tracking
        if (request.getUserId() != null) {
            formData.add("custom.user_id", request.getUserId().toString());
        }
        formData.add("custom.template", request.getTemplate());
        
        log.debug("Created Gupshup form data for recipient: {} with template: {}", 
                 request.getRecipient(), request.getTemplate());
        
        return formData;
    }

    /**
     * Processes Gupshup API response.
     * 
     * @param notificationId notification ID
     * @param userId user ID
     * @param responseBody response body
     * @return notification response
     */
    private NotificationResponse processResponse(UUID notificationId, UUID userId, String responseBody) {
        try {
            log.debug("Gupshup response: {}", responseBody);
            
            JsonNode responseJson = objectMapper.readTree(responseBody);
            
            // Check if response indicates success
            if (responseJson.has("response") && responseJson.get("response").has("id")) {
                // Success
                String messageId = responseJson.get("response").get("id").asText();
                
                log.info("SMS sent successfully via Gupshup - MessageId: {}", messageId);
                
                return NotificationResponse.builder()
                        .notificationId(notificationId)
                        .userId(userId)
                        .status(NotificationResponse.NotificationStatus.SENT)
                        .message("SMS sent successfully")
                        .providerResponse(responseBody)
                        .providerMessageId(messageId)
                        .sentAt(java.time.Instant.now())
                        .retryCount(0)
                        .build();
            } else {
                // Check for error
                String errorMessage = "Unknown error";
                if (responseJson.has("response") && responseJson.get("response").has("details")) {
                    errorMessage = responseJson.get("response").get("details").asText();
                }
                
                log.warn("Failed to send SMS via Gupshup - Error: {}", errorMessage);
                
                return NotificationResponse.failure(notificationId, userId, 
                    "Gupshup error: " + errorMessage, 0);
            }
            
        } catch (Exception e) {
            log.error("Failed to parse Gupshup response: {}", responseBody, e);
            return NotificationResponse.failure(notificationId, userId, 
                "Failed to parse Gupshup response", 0);
        }
    }
}
