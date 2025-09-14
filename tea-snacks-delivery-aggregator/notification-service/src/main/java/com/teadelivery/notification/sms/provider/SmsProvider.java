package com.teadelivery.notification.sms.provider;

import com.teadelivery.notification.shared.dto.NotificationRequest;
import com.teadelivery.notification.shared.dto.NotificationResponse;

/**
 * SMS provider interface for sending SMS messages.
 * Follows coding standards with provider abstraction.
 */
public interface SmsProvider {

    /**
     * Sends SMS notification.
     * 
     * @param request notification request
     * @param content rendered SMS content
     * @return notification response
     */
    NotificationResponse sendSms(NotificationRequest request, String content);

    /**
     * Validates SMS configuration.
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
