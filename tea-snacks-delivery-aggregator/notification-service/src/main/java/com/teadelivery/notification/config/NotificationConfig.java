package com.teadelivery.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for notification service.
 * Follows coding standards with comprehensive notification setup.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationConfig {

    private Email email = new Email();
    private Sms sms = new Sms();
    private Async async = new Async();
    private Templates templates = new Templates();

    @Data
    public static class Email {
        private String provider = "sendgrid";
        private String apiKey;
        private String fromEmail = "noreply@teasnacks.com";
        private String fromName = "Tea & Snacks";
        private int maxRetries = 3;
        private long retryDelay = 5000; // milliseconds
        private RateLimit rateLimit = new RateLimit();
    }

    @Data
    public static class Sms {
        private String provider = "gupshup";
        private String apiKey;
        private String senderId = "TEASNK";
        private String baseUrl = "https://api.gupshup.io/sm/api/v1";
        private int maxRetries = 3;
        private long retryDelay = 5000; // milliseconds
        private RateLimit rateLimit = new RateLimit();
    }

    @Data
    public static class RateLimit {
        private int max = 100;
        private String per = "minute"; // minute, hour, day
    }

    @Data
    public static class Async {
        private int threadPoolSize = 5;
        private int queueCapacity = 100;
        private String threadNamePrefix = "notification-";
    }

    @Data
    public static class Templates {
        private String location = "classpath:/templates/notifications/";
        private String emailPath = "email/";
        private String smsPath = "sms/";
    }
}
