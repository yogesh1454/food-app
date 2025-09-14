package com.teadelivery.notification.email.provider;

import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;

/**
 * Email provider interface for sending emails.
 * Follows coding standards with provider abstraction.
 */
public interface EmailProvider {

    /**
     * Sends email notification.
     * 
     * @param request notification request
     * @param content rendered email content
     * @return notification response
     */
    NotificationResponse sendEmail(NotificationRequest request, String content);

    /**
     * Validates email configuration.
     * 
     * @return true if configuration is valid
     */
    boolean isConfigurationValid();

    /**
     * Gets provider name.
     * 
     * @return provider name
     */
    String getProviderName();
}
