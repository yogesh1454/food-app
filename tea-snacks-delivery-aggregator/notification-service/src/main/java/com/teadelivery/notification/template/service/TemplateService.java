package com.teadelivery.notification.template.service;

import com.teadelivery.notification.config.NotificationConfig;
import com.teadelivery.notification.shared.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Template service for rendering notification templates.
 * Follows coding standards with comprehensive template processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateEngine templateEngine;
    private final NotificationConfig notificationConfig;

    /**
     * Renders email template with variables.
     * 
     * @param templateName template name
     * @param variables template variables
     * @return rendered content
     */
    public String renderEmailTemplate(String templateName, Map<String, Object> variables) {
        try {
            String templatePath = notificationConfig.getTemplates().getEmailPath() + templateName;
            
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            
            String renderedContent = templateEngine.process(templatePath, context);
            
            log.debug("Rendered email template: {} with {} variables", templateName, 
                     variables != null ? variables.size() : 0);
            
            return renderedContent;
            
        } catch (Exception e) {
            log.error("Failed to render email template: {}", templateName, e);
            return getDefaultEmailContent(templateName, variables);
        }
    }

    /**
     * Renders SMS template with variables.
     * 
     * @param templateName template name
     * @param variables template variables
     * @return rendered content
     */
    public String renderSmsTemplate(String templateName, Map<String, Object> variables) {
        try {
            String templatePath = notificationConfig.getTemplates().getSmsPath() + templateName;
            
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            
            String renderedContent = templateEngine.process(templatePath, context);
            
            log.debug("Rendered SMS template: {} with {} variables", templateName, 
                     variables != null ? variables.size() : 0);
            
            return renderedContent;
            
        } catch (Exception e) {
            log.error("Failed to render SMS template: {}", templateName, e);
            return getDefaultSmsContent(templateName, variables);
        }
    }

    /**
     * Renders template based on notification type.
     * 
     * @param request notification request
     * @return rendered content
     */
    public String renderTemplate(NotificationRequest request) {
        switch (request.getType()) {
            case EMAIL:
                return renderEmailTemplate(request.getTemplate(), request.getVariables());
            case SMS:
                return renderSmsTemplate(request.getTemplate(), request.getVariables());
            default:
                log.warn("Unsupported notification type for template rendering: {}", request.getType());
                return "Notification content";
        }
    }

    /**
     * Validates if template exists.
     * 
     * @param templateName template name
     * @param type notification type
     * @return true if template exists
     */
    public boolean templateExists(String templateName, NotificationRequest.NotificationType type) {
        try {
            String templatePath = getTemplatePath(templateName, type);
            // Try to process template with empty context to check if it exists
            templateEngine.process(templatePath, new Context());
            return true;
        } catch (Exception e) {
            log.debug("Template does not exist: {} for type: {}", templateName, type);
            return false;
        }
    }

    /**
     * Gets template path based on type.
     * 
     * @param templateName template name
     * @param type notification type
     * @return template path
     */
    private String getTemplatePath(String templateName, NotificationRequest.NotificationType type) {
        switch (type) {
            case EMAIL:
                return notificationConfig.getTemplates().getEmailPath() + templateName;
            case SMS:
                return notificationConfig.getTemplates().getSmsPath() + templateName;
            default:
                return templateName;
        }
    }

    /**
     * Gets default email content when template fails.
     * 
     * @param templateName template name
     * @param variables template variables
     * @return default content
     */
    private String getDefaultEmailContent(String templateName, Map<String, Object> variables) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>Tea & Snacks Notification</h2>");
        content.append("<p>This is a notification from Tea & Snacks delivery service.</p>");
        
        if (variables != null && !variables.isEmpty()) {
            content.append("<p>Details:</p><ul>");
            variables.forEach((key, value) -> 
                content.append("<li>").append(key).append(": ").append(value).append("</li>"));
            content.append("</ul>");
        }
        
        content.append("<p>Template: ").append(templateName).append("</p>");
        content.append("</body></html>");
        
        log.info("Using default email content for template: {}", templateName);
        return content.toString();
    }

    /**
     * Gets default SMS content when template fails.
     * 
     * @param templateName template name
     * @param variables template variables
     * @return default content
     */
    private String getDefaultSmsContent(String templateName, Map<String, Object> variables) {
        StringBuilder content = new StringBuilder();
        content.append("Tea & Snacks: ");
        
        if (variables != null && variables.containsKey("message")) {
            content.append(variables.get("message"));
        } else if (variables != null && variables.containsKey("code")) {
            content.append("Your verification code is: ").append(variables.get("code"));
        } else {
            content.append("You have a new notification.");
        }
        
        log.info("Using default SMS content for template: {}", templateName);
        return content.toString();
    }
}
